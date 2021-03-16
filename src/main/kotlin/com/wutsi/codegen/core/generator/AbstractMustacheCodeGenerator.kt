package com.wutsi.codegen.core.generator

import com.github.mustachejava.DefaultMustacheFactory
import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI
import java.io.File
import java.io.FileWriter
import java.io.InputStreamReader

abstract class AbstractMustacheCodeGenerator : CodeGenerator {

    abstract fun toMustacheScope(openAPI: OpenAPI, context: Context): Map<String, String?>

    protected open fun canGenerate(file: File): Boolean =
        !file.exists()

    protected fun generate(inputPath: String, outputFile: File, openAPI: OpenAPI, context: Context) {
        if (!canGenerate(outputFile))
            return

        val reader = InputStreamReader(AbstractPomCodeGenerator::class.java.getResourceAsStream(inputPath))
        reader.use {
            System.out.println("Generating $outputFile")
            outputFile.parentFile.mkdirs()
            val writer = FileWriter(outputFile)
            writer.use {
                val mustache = DefaultMustacheFactory().compile(reader, "text")
                val scope = toMustacheScope(openAPI, context)
                mustache.execute(
                    writer,
                    mapOf(
                        "scope" to scope
                    )
                )
                writer.flush()
            }
        }
    }
}
