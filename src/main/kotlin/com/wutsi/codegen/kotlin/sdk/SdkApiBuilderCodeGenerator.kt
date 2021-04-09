package com.wutsi.codegen.kotlin.sdk

import com.fasterxml.jackson.databind.ObjectMapper
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.TypeSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Api
import io.swagger.v3.oas.models.OpenAPI

class SdkApiBuilderCodeGenerator(private val mapper: KotlinMapper) : AbstractKotlinCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val api = mapper.toAPI(openAPI)
        generateAPIBuilder(api, context)
    }

    private fun generateAPIBuilder(api: Api, context: Context) {
        val file = getSourceDirectory(context)
        val classname = "${api.name}Builder"
        System.out.println("Generating ${api.packageName}.$classname to $file")

        FileSpec.builder(api.packageName, classname)
            .addType(toTypeSpec(api, context))
            .build()
            .writeTo(file)
    }

    fun toTypeSpec(api: Api, context: Context): TypeSpec {
        val spec = TypeSpec.classBuilder(api.name + "Builder")
            .addFunction(toFunSpec(api, context))
            .build()
        return spec
    }

    private fun toFunSpec(api: Api, context: Context): FunSpec {
        return FunSpec.builder("build")
            .addModifiers(PUBLIC)
            .addParameter("env", SdkEnvironmentGenerator().toClassname(context))
            .addParameter("mapper", ObjectMapper::class)
            .addCode(
                CodeBlock.of(
                    """
                        return feign.Feign.builder()
                          .client(feign.okhttp.OkHttpClient())
                          .encoder(feign.jackson.JacksonEncoder(mapper))
                          .decoder(feign.jackson.JacksonDecoder(mapper))
                          .logger(feign.slf4j.Slf4jLogger())
                          .target(${api.name}::class.java, env.url)
                    """.trimIndent()
                )
            )
            .build()
    }
}
