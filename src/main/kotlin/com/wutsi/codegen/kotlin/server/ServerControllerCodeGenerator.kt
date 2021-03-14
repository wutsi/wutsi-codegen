package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Endpoint
import com.wutsi.codegen.model.EndpointParameter
import com.wutsi.codegen.model.ParameterType.HEADER
import com.wutsi.codegen.model.ParameterType.PATH
import com.wutsi.codegen.model.ParameterType.QUERY
import com.wutsi.codegen.model.Request
import com.wutsi.codegen.util.CaseUtil
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

    override fun parameterAnnotations(parameter: EndpointParameter): List<AnnotationSpec> =
        toAnnotationSpecs(parameter)

    override fun constructorSpec(endpoint: Endpoint, context: Context): FunSpec {
        val delegate = ServerDelegateCodeGenerator(mapper)
        return FunSpec.constructorBuilder()
            .addParameter(
                DELEGATE_VARIABLE,
                ClassName(
                    delegate.packageName(endpoint, context),
                    delegate.className(endpoint),
                )
            )
            .build()
    }

    override fun funCodeBloc(endpoint: Endpoint): CodeBlock {
        val params = mutableListOf<String>()
        params.add(REQUEST_VARIABLE)
        endpoint.parameters.forEach { params.add(it.field.name) }

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

    override fun toParameter(parameter: EndpointParameter): ParameterSpec {
        return ParameterSpec.builder(parameter.field.name, parameter.field.type)
            .addAnnotation(toAnnotationSpec(parameter))
            .addAnnotations(toValidationAnnotationSpecs(parameter.field))
            .build()
    }

    fun toAnnotationSpecs(parameter: EndpointParameter): List<AnnotationSpec> {
        val result = mutableListOf<AnnotationSpec>()
        result.add(toAnnotationSpec(parameter))
        result.addAll(super.toValidationAnnotationSpecs(parameter.field))
        return result
    }

    private fun toAnnotationSpec(parameter: EndpointParameter): AnnotationSpec {
        val builder = AnnotationSpec.builder(toParameterType(parameter))
            .addMember("name=%S", parameter.name)
        if (parameter.type != PATH)
            builder.addMember("required=%S", parameter.field.required)
        if (parameter.field.default != null)
            builder.addMember("default=%S", parameter.field.default)
        return builder.build()
    }

    fun toParameterType(parameter: EndpointParameter): KClass<out Annotation> =
        when (parameter.type) {
            PATH -> PathVariable::class
            QUERY -> RequestParam::class
            HEADER -> RequestHeader::class
            else -> throw IllegalStateException("Parameter type not supported: ${parameter.type}")
        }
}
