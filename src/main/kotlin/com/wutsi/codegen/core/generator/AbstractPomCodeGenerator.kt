package com.wutsi.codegen.core.generator

import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

abstract class AbstractPomCodeGenerator : AbstractMustacheCodeGenerator() {
    protected abstract fun getTemplatePath(): String

    override fun generate(openAPI: OpenAPI, context: Context) {
        generate(
            inputPath = getTemplatePath(),
            outputFile = File(context.outputDirectory, "pom.xml"),
            openAPI = openAPI,
            context = context
        )
    }
}
