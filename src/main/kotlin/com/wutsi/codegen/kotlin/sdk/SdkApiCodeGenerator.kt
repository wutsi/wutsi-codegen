package com.wutsi.codegen.kotlin.sdk

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ABSTRACT
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Api
import com.wutsi.codegen.model.Endpoint
import com.wutsi.codegen.model.EndpointParameter
import com.wutsi.codegen.model.ParameterType.QUERY
import feign.Headers
import feign.Param
import feign.RequestLine
import io.swagger.v3.oas.models.OpenAPI

class SdkApiCodeGenerator(private val mapper: KotlinMapper) : AbstractKotlinCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val api = mapper.toAPI(openAPI)
        generateAPI(api, context)
    }

    private fun generateAPI(api: Api, context: Context) {
        val file = getSourceDirectory(context)
        System.out.println("Generating ${api.packageName}.${api.name} to $file")

        FileSpec.builder(api.packageName, api.name)
            .addType(toTypeSpec(api))
            .build()
            .writeTo(file)
    }

    fun toTypeSpec(api: Api): TypeSpec {
        val spec = TypeSpec.interfaceBuilder(api.name)
            .addFunctions(api.endpoints.map { toFunSpec(it) })
            .build()
        return spec
    }

    private fun toFunSpec(endpoint: Endpoint): FunSpec {
        val builder = FunSpec.builder(endpoint.name)
            .addModifiers(ABSTRACT)
            .addAnnotation(
                AnnotationSpec.builder(RequestLine::class)
                    .addMember("%S", requestLine(endpoint))
                    .build()
            )
            .addAnnotation(
                AnnotationSpec.builder(Headers::class.java)
                    .addMember("%S", "Content-Type: application/json")
                    .build()
            )
            .addParameters(endpoint.parameters.map { toParameter(it) })

        if (endpoint.request != null) {
            val type = endpoint.request.type
            builder.addParameter(
                ParameterSpec
                    .builder("request", ClassName(type.packageName, type.name))
                    .build()
            )
        }

        if (endpoint.response != null)
            builder.returns(ClassName(endpoint.response.packageName, endpoint.response.name))

        return builder.build()
    }

    private fun requestLine(endpoint: Endpoint): String {
        val line = StringBuilder("${endpoint.method} ${endpoint.path}")
        val queryParams = endpoint.parameters.filter { it.type == QUERY }
        if (queryParams.isNotEmpty()) {
            line.append("?")
                .append(
                    queryParams
                        .map { "${it.name}={${it.name}}" }
                        .joinToString("&")
                )
        }
        return line.toString()
    }

    private fun toParameter(parameter: EndpointParameter): ParameterSpec {
        val builder = ParameterSpec.builder(parameter.field.name, parameter.field.type.asTypeName().copy(parameter.field.nullable))
            .addAnnotation(
                AnnotationSpec.builder(Param::class)
                    .addMember("\"${parameter.name}\"")
                    .build()
            )

        val defaultValue = defaultValue(parameter.field)
        if (defaultValue != null) {
            builder.defaultValue(defaultValue)
        }

        return builder.build()
    }
}
