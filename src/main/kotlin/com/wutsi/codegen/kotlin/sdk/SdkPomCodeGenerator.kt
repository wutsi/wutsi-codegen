package com.wutsi.codegen.kotlin.sdk

import com.github.mustachejava.DefaultMustacheFactory
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.util.CaseUtil
import io.swagger.v3.oas.models.OpenAPI
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

class SdkPomCodeGenerator(private val mapper: KotlinMapper) : AbstractKotlinCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val pom = toPom(openAPI, context)
        val reader = InputStreamReader(SdkPomCodeGenerator::class.java.getResourceAsStream("/kotlin/sdk/pom.xml.mustache"))
        reader.use {
            val writer = FileWriter(File(context.outputDirectory, "pom.xml"))
            writer.use {
                val mustache = DefaultMustacheFactory().compile(reader, "text")
                mustache.execute(writer, mapOf("pom" to pom))
                writer.flush()
            }
        }
    }

    fun toPom(openAPI: OpenAPI, context: Context) = mapOf(
        "artifactId" to artifactId(context),
        "groupId" to context.basePackage,
        "jdkVersion" to context.jdkVersion,
        "version" to openAPI.info?.version
    )

    fun artifactId(context: Context): String =
        CaseUtil.toSnakeCase(context.apiName.toLowerCase()) + "-sdk"
}
