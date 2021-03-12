package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Endpoint
import com.wutsi.codegen.model.EndpointParameter
import com.wutsi.codegen.model.Request
import io.swagger.v3.oas.models.OpenAPI

abstract class AbstractServerCodeGenerator(private val mapper: KotlinMapper) : AbstractKotlinCodeGenerator() {
    protected abstract fun className(endpoint: Endpoint): String

    protected abstract fun packageName(endpoint: Endpoint, context: Context): String

    protected abstract fun classAnnotations(endpoint: Endpoint): List<AnnotationSpec>

    protected abstract fun functionAnnotations(endpoint: Endpoint): List<AnnotationSpec>

    protected abstract fun requestBodyAnnotations(requestBody: Request?): List<AnnotationSpec>

    protected abstract fun parameterAnnotations(parameter: EndpointParameter): List<AnnotationSpec>

    override fun generate(openAPI: OpenAPI, context: Context) {
        val api = mapper.toAPI(openAPI)
        api.endpoints.forEach { generateController(it, context) }
    }

    fun generateController(endpoint: Endpoint, context: Context) {
        val file = getSourceDirectory(context)
        val packageName = packageName(endpoint, context)
        val classname = className(endpoint)
        System.out.println("Generating $packageName.$classname to $file")

        FileSpec.builder(packageName, classname)
            .addType(toTypeSpec(endpoint))
            .build()
            .writeTo(file)
    }

    fun toTypeSpec(endpoint: Endpoint): TypeSpec {
        val spec = TypeSpec.classBuilder(className(endpoint))
            .addAnnotations(classAnnotations(endpoint))
            .addFunction(toFunSpec(endpoint))
            .build()
        return spec
    }

    fun toFunSpec(endpoint: Endpoint): FunSpec {
        val builder = FunSpec.builder("invoke")
            .addAnnotations(functionAnnotations(endpoint))
            .addParameters(endpoint.parameters.map { toParameter(it) })

        if (endpoint.request != null) {
            val type = endpoint.request.type
            builder.addParameter(
                ParameterSpec
                    .builder("request", ClassName(type.packageName, type.name))
                    .addAnnotations(requestBodyAnnotations(endpoint.request))
                    .build()
            )
        }

        if (endpoint.response != null)
            builder.returns(ClassName(endpoint.response.packageName, endpoint.response.name))

        return builder.build()
    }

    open fun toParameter(parameter: EndpointParameter): ParameterSpec {
        return ParameterSpec.builder(parameter.field.name, parameter.field.type)
            .addAnnotations(parameterAnnotations(parameter))
            .build()
    }

    protected fun toPackage(basePackage: String, suffix: String): String =
        if (basePackage.isNullOrEmpty()) suffix else "$basePackage.$suffix"
}
