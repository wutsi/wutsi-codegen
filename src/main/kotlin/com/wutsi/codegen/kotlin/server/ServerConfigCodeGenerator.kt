package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractMustacheCodeGenerator
import com.wutsi.codegen.core.util.CaseUtil
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

class ServerConfigCodeGenerator : AbstractMustacheCodeGenerator() {
    override fun toMustacheScope(openAPI: OpenAPI, context: Context) = mapOf(
        "services" to toServices(context)
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
        generate("application.yml", openAPI, context)
        generate("application-test.yml", openAPI, context)
        generate("application-prod.yml", openAPI, context)
    }

    private fun generate(filename: String, spec: OpenAPI, context: Context) {
        generate(
            inputPath = "/kotlin/server/$filename.mustache",
            outputFile = File("${context.outputDirectory}/src/main/resources/$filename"),
            openAPI = spec,
            context = context
        )
    }
}
