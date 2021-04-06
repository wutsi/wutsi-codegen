package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractMustacheCodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

class SdkReadmeCodeGenerator() : AbstractMustacheCodeGenerator() {
    override fun toMustacheScope(openAPI: OpenAPI, context: Context) = mapOf(
        "apiName" to context.apiName,
        "githubProject" to context.githubProject,
        "description" to openAPI.info.description,
        "githubUser" to context.githubUser,
        "jdkVersion" to context.jdkVersion
    )

    override fun canGenerate(file: File) = !file.exists()

    override fun generate(openAPI: OpenAPI, context: Context) {
        if (context.githubUser == null || context.githubProject == null)
            return

        generate(
            inputPath = "/kotlin/sdk/README.md.mustache",
            outputFile = File("${context.outputDirectory}/README.md"),
            openAPI = openAPI,
            context = context
        )
    }
}
