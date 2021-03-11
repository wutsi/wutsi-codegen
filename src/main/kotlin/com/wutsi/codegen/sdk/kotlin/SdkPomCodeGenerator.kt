package com.wutsi.codegen.sdk.kotlin

import com.github.mustachejava.DefaultMustacheFactory
import com.wutsi.codegen.CodeGenerator
import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

class SdkPomCodeGenerator(private val mapper: KotlinMapper) : CodeGenerator {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val pom = mapper.toPom(openAPI)
        val reader = InputStreamReader(SdkPomCodeGenerator::class.java.getResourceAsStream("/sdk/kotlin/pom.xml.mustache"))
        reader.use {
            val writer = FileWriter(File(context.outputDirectory + "${File.separator}pom.xml"))
            writer.use {
                val mustache = DefaultMustacheFactory().compile(reader, "text")
                mustache.execute(writer, mapOf("pom" to pom))
                writer.flush()
            }
        }
    }
}
