package com.wutsi.codegen.core.generator

import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream

abstract class AbstractStaticCodeGenerator : CodeGenerator {

    protected open fun canGenerate(file: File): Boolean =
        !file.exists()

    protected open fun generate(inputPath: String, outputFile: File, openAPI: OpenAPI, context: Context) {
        if (!canGenerate(outputFile))
            return

        val input = AbstractStaticCodeGenerator::class.java.getResourceAsStream(inputPath)
        input.use {
            System.out.println("Generating $outputFile")
            outputFile.parentFile.mkdirs()
            val output = FileOutputStream(outputFile)
            output.use {
                IOUtils.copy(input, output)
            }
        }
    }
}
