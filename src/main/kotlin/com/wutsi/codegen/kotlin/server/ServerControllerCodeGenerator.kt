package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Api
import com.wutsi.codegen.model.Endpoint
import com.wutsi.codegen.util.CaseUtil.toCamelCase
import io.swagger.v3.oas.models.OpenAPI
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

class ServerControllerCodeGenerator(private val mapper: KotlinMapper) : AbstractKotlinCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val api = mapper.toAPI(openAPI)
        api.endpoints.forEach { generateApi(api, it, context) }
    }

    fun generateApi(api: Api, endpoint: Endpoint, context: Context) {
        val file = getSourceDirectory(context)
        val classname = controllerClassName(endpoint)
        System.out.println("Generating ${api.packageName}.$classname to $file")

        FileSpec.builder(api.packageName, classname)
            .addType(toTypeSpec(endpoint, context))
            .build()
            .writeTo(file)
    }

    fun toTypeSpec(endpoint: Endpoint, context: Context): TypeSpec {
        val spec = TypeSpec.classBuilder(controllerClassName(endpoint))
            .addAnnotation(
                AnnotationSpec.builder(RestController::class)
                    .build()
            )
            .addFunction(toFunSpec(endpoint, context))
            .build()
        return spec
    }

    fun toFunSpec(endpoint: Endpoint, context: Context): FunSpec {
        val builder = FunSpec.builder("invoke")
            .addAnnotation(
                AnnotationSpec.builder(RequestMapping::class)
                    .addMember("value=%S", endpoint.path)
                    .addMember("method=%S", toHttpMethod(endpoint))
                    .build()
            )


        if (endpoint.response != null)
            builder.returns(ClassName(endpoint.response.packageName, endpoint.response.name))

        return builder.build()
    }

    private fun toHttpMethod(endpoint: Endpoint): HttpMethod =
        when (endpoint.method.toUpperCase()) {
            "POST" -> HttpMethod.POST
            "PUT" -> HttpMethod.PUT
            "DELETE" -> HttpMethod.DELETE
            "TRACE" -> HttpMethod.TRACE
            "OPTIONS" -> HttpMethod.OPTIONS
            else -> HttpMethod.GET
        }

    private fun controllerClassName(endpoint: Endpoint): String =
        toCamelCase("${endpoint.name}Controller", true)
}
