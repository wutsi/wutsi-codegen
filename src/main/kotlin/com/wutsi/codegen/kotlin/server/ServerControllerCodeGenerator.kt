package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.AnnotationSpec
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
import javax.validation.Valid
import kotlin.reflect.KClass

class ServerControllerCodeGenerator(mapper: KotlinMapper) : AbstractServerCodeGenerator(mapper) {
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
        emptyList()

//    override fun generate(openAPI: OpenAPI, context: Context) {
//        val api = mapper.toAPI(openAPI)
//        api.endpoints.forEach { generateController(api, it, context) }
//    }
//
//    fun generateController(api: Api, endpoint: Endpoint, context: Context) {
//        val file = getSourceDirectory(context)
//        val classname = controllerClassName(endpoint)
//        System.out.println("Generating ${api.packageName}.$classname to $file")
//
//        FileSpec.builder(api.packageName, classname)
//            .addType(toTypeSpec(endpoint))
//            .build()
//            .writeTo(file)
//    }

//    fun toTypeSpec(endpoint: Endpoint): TypeSpec {
//        val spec = TypeSpec.classBuilder(controllerClassName(endpoint))
//            .addAnnotation(
//                AnnotationSpec.builder(RestController::class)
//                    .build()
//            )
//            .addFunction(toFunSpec(endpoint))
//            .build()
//        return spec
//    }

//    fun toFunSpec(endpoint: Endpoint): FunSpec {
//        val builder = FunSpec.builder("invoke")
//            .addAnnotation(
//                AnnotationSpec.builder(toRequestMappingClass(endpoint))
//                    .addMember("%S", endpoint.path)
//                    .build()
//            )
//            .addParameters(endpoint.parameters.map { toParameter(it) })
//
//        if (endpoint.request != null) {
//            val type = endpoint.request.type
//            builder.addParameter(
//                ParameterSpec
//                    .builder("request", ClassName(type.packageName, type.name))
//                    .addAnnotation(Valid::class)
//                    .addAnnotation(RequestBody::class)
//                    .build()
//            )
//        }
//
//        if (endpoint.response != null)
//            builder.returns(ClassName(endpoint.response.packageName, endpoint.response.name))
//
//        return builder.build()
//    }

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
