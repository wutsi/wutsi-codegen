package com.wutsi.codegen.kotlin

import com.github.mustachejava.DefaultMustacheFactory
import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

abstract class AbstractPomCodeGenerator(protected val mapper: KotlinMapper) : AbstractKotlinCodeGenerator() {
    protected abstract fun getTemplatePath(): String

    protected abstract fun toPom(openAPI: OpenAPI, context: Context): Map<String, String>

    protected abstract fun canGenerate(context: Context): Boolean

    override fun generate(openAPI: OpenAPI, context: Context) {
        if (!canGenerate(context))
            return

        val pom = toPom(openAPI, context)
        val reader = InputStreamReader(AbstractPomCodeGenerator::class.java.getResourceAsStream(getTemplatePath()))
        reader.use {
            val file = File(context.outputDirectory, "pom.xml")

            System.out.println("Generating $file")
            file.parentFile.mkdirs()
            val writer = FileWriter(file)
            writer.use {
                val mustache = DefaultMustacheFactory().compile(reader, "text")
                mustache.execute(writer, mapOf("pom" to pom))
                writer.flush()
            }
        }
    }
}
