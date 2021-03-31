package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractMustacheCodeGenerator
import com.wutsi.codegen.core.util.CaseUtil
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

class ServerReadmeCodeGenerator() : AbstractMustacheCodeGenerator() {
    override fun toMustacheScope(openAPI: OpenAPI, context: Context) = mapOf(
        "services" to toServices(context),
        "setupDatabase" to context.hasService(Context.SERVICE_DATABASE),
        "setupGithub" to (context.hasService(Context.SERVICE_MQUEUE) || context.hasService(Context.SERVICE_DATABASE)),
        "githubProject" to context.githubProject,
        "description" to openAPI.info.description,
        "githubUser" to context.githubUser,
        "jdkVersion" to context.jdkVersion
    )

    override fun canGenerate(file: File) = !file.exists()

    private fun toServices(context: Context): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        if (context.hasService(Context.SERVICE_DATABASE)) {
            result["database"] = true
            result["databaseName"] = CaseUtil.toSnakeCase(context.apiName).toLowerCase()
        }
        if (context.hasService(Context.SERVICE_CACHE)) {
            result["cache"] = true
        }
        if (context.hasService(Context.SERVICE_MQUEUE)) {
            result["mqueue"] = true
        }
        return result
    }

    override fun generate(openAPI: OpenAPI, context: Context) {
        if (context.githubUser == null || context.githubUser == null)
            return

        generate(
            inputPath = "/kotlin/server/README.md.mustache",
            outputFile = File("${context.outputDirectory}/README.md"),
            openAPI = openAPI,
            context = context
        )
    }
}
