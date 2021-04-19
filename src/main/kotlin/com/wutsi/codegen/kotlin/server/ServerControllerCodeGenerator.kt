package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.core.util.CaseUtil
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Endpoint
import com.wutsi.codegen.model.EndpointParameter
import com.wutsi.codegen.model.ParameterType.HEADER
import com.wutsi.codegen.model.ParameterType.PATH
import com.wutsi.codegen.model.ParameterType.QUERY
import com.wutsi.codegen.model.Request
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File
import javax.validation.Valid
import kotlin.reflect.KClass

class ServerControllerCodeGenerator(mapper: KotlinMapper) : AbstractServerCodeGenerator(mapper) {
    companion object {
        const val DELEGATE_VARIABLE = "delegate"
    }

    override fun className(endpoint: Endpoint): String =
        CaseUtil.toCamelCase("${endpoint.name}Controller", true)

    override fun packageName(endpoint: Endpoint, context: Context): String =
        toPackage(context.basePackage, "endpoint")

    override fun classAnnotations(endpoint: Endpoint): List<AnnotationSpec> =
        listOf(
            AnnotationSpec.builder(RestController::class)
                .build()
        )

    override fun functionAnnotations(endpoint: Endpoint): List<AnnotationSpec> =
        listOf(
            AnnotationSpec.builder(toRequestMappingClass(endpoint))
                .addMember("%S", endpoint.path)
                .build()
        )

    override fun requestBodyAnnotations(requestBody: Request?): List<AnnotationSpec> =
        listOf(
            AnnotationSpec.builder(Valid::class).build(),
            AnnotationSpec.builder(RequestBody::class).build()
        )

    override fun parameterAnnotations(parameter: EndpointParameter, getter: Boolean): List<AnnotationSpec> {
        val default = defaultValue(parameter.field)
        val result = mutableListOf<AnnotationSpec>()
        result.add(toAnnotationSpec(parameter, default))
        result.addAll(super.toValidationAnnotationSpecs(parameter.field, getter))
        return result
    }

    override fun constructorSpec(endpoint: Endpoint, context: Context): FunSpec {
        val delegate = ServerDelegateCodeGenerator(mapper)
        return FunSpec.constructorBuilder()
            .addParameter(
                ParameterSpec(
                    DELEGATE_VARIABLE,
                    ClassName(
                        delegate.packageName(endpoint, context),
                        delegate.className(endpoint),
                    ),
                    KModifier.PRIVATE
                )
            )
            .build()
    }

    override fun funCodeBloc(endpoint: Endpoint): CodeBlock {
        val params = mutableListOf<String>()
        endpoint.parameters.forEach { params.add(it.field.name) }
        if (endpoint.request != null)
            params.add(REQUEST_VARIABLE)

        val statement = "$DELEGATE_VARIABLE.$INVOKE_FUNCTION(" + params.joinToString() + ")"
        val builder = CodeBlock.builder()
        if (endpoint.response == null)
            builder.addStatement(statement)
        else
            builder.addStatement("return $statement")
        return builder.build()
    }

    override fun canGenerate(directory: File, packageName: String, className: String): Boolean = true

    fun toRequestMappingClass(endpoint: Endpoint): KClass<out Annotation> =
        when (endpoint.method.toUpperCase()) {
            "POST" -> PostMapping::class
            "PUT" -> PutMapping::class
            "DELETE" -> DeleteMapping::class
            "GET" -> GetMapping::class
            else -> throw IllegalStateException("Method not supported: ${endpoint.method}")
        }

    private fun toAnnotationSpec(parameter: EndpointParameter, default: String?): AnnotationSpec {
        val builder = AnnotationSpec.builder(toParameterType(parameter))
            .addMember("name=%S", parameter.name)
        if (parameter.type != PATH) {
            builder.addMember("required=" + parameter.field.required)
            if (default != null && default != "null")
                builder.addMember("defaultValue=%S", default)
        }
        return builder.build()
    }

    fun toParameterType(parameter: EndpointParameter): KClass<out Annotation> =
        when (parameter.type) {
            PATH -> PathVariable::class
            QUERY -> RequestParam::class
            HEADER -> RequestHeader::class
            else -> throw IllegalStateException("Parameter type not supported: ${parameter.type}")
        }

    override fun generateTest(endpoint: Endpoint, context: Context) {
        val directory = getTestDirectory(context)
        val packageName = packageName(endpoint, context)
        val classname = className(endpoint) + "Test"
        val file = File(
            directory.absolutePath +
                "/$packageName.$classname".replace('.', '/') +
                ".kt"
        )
        if (file.exists()) {
            return
        }

        System.out.println("Generating $file")
        FileSpec.builder(packageName, classname)
            .addType(
                TypeSpec.classBuilder(ClassName(packageName, classname))
                    .addAnnotation(
                        AnnotationSpec.builder(SpringBootTest::class)
                            .addMember("webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT")
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("port", Int::class)
                            .addAnnotation(LocalServerPort::class)
                            .initializer("0")
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("invoke")
                            .addAnnotation(Test::class)
                            .build()
                    )
                    .build()
            )
            .build()
            .writeTo(directory)
    }
}
