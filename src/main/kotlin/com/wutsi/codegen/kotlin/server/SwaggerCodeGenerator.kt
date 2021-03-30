package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.CodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import java.io.File
import java.nio.file.Files

class SwaggerCodeGenerator : CodeGenerator {
    companion object {
        private val SWAGGER_VERSION = "3.45.1"
    }

    override fun generate(openAPI: OpenAPI, context: Context) {
        val directory = outpuDirectory(context)
        copyFiles(directory)
        copySource(context, directory)
    }

    private fun outpuDirectory(context: Context): File {
        val directory = File(context.outputDirectory + File.separator + "docs" + File.separator + "api")
        if (!directory.exists())
            directory.mkdirs()
        return directory
    }

    private fun copyFiles(directory: File) {
        val filenames = listOf(
            "favicon-16x16.png",
            "favicon-32x32.png",
            "index.html",
            "oauth2-redirect.html",
            "swagger-ui.css",
            "swagger-ui.js",
            "swagger-ui-bundle.js",
            "swagger-ui-standalone-preset.js"
        )

        filenames.forEach {
            val input = SwaggerCodeGenerator::class.java.getResourceAsStream("/swagger/$SWAGGER_VERSION/$it")
            val output = File(directory, it)

            System.out.println("Copying $it to $output")
            if (output.exists())
                output.delete()
            Files.copy(input, output.toPath())
        }
    }

    private fun copySource(context: Context, directory: File) {
        // Download and copy the file
        val file = File(directory, "api.yaml")
        if (file.exists())
            file.delete()
        val input = context.inputUrl?.openStream()
        input.use {
            Files.copy(it, file.toPath())
        }

        // Change the base URL
        val index = File(directory, "index.html").toPath()
        val content = Files.readString(index)
        Files.writeString(
            index,
            content.replace("https://petstore.swagger.io/v2/swagger.json", "./api.yaml")
        )
    }
}
