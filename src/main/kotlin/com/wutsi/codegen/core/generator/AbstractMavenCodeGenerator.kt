package com.wutsi.codegen.core.generator

import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

abstract class AbstractMavenCodeGenerator : AbstractMustacheCodeGenerator() {
    protected abstract fun getTemplatePath(): String

    override fun canGenerate(file: File) = !file.exists()

    override fun generate(openAPI: OpenAPI, context: Context) {
        generate(
            inputPath = getTemplatePath(),
            outputFile = File(context.outputDirectory, "pom.xml"),
            openAPI = openAPI,
            context = context
        )

        if (context.githubUser != null) {
            generate(
                inputPath = "/settings.xml.mustache",
                outputFile = File(context.outputDirectory, "settings.xml"),
                openAPI = openAPI,
                context = context
            )
        }
    }
}
