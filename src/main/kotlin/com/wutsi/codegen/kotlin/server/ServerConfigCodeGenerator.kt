package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractMustacheCodeGenerator
import com.wutsi.codegen.core.util.CaseUtil
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

class ServerConfigCodeGenerator(private val mapper: KotlinMapper) : AbstractMustacheCodeGenerator() {
    override fun toMustacheScope(openAPI: OpenAPI, context: Context) = mapOf(
        "services" to toServices(context)
    )

    private fun toServices(context: Context): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        if (context.hasService(Context.SERVICE_DATABASE)) {
            result["database"] = true
            result["databaseName"] = CaseUtil.toSnakeCase(context.apiName)
        }
        if (context.hasService(Context.SERVICE_CACHE)) {
            result["cache"] = true
        }
        return result
    }

    override fun generate(spec: OpenAPI, context: Context) {
        generate("application.yml", spec, context)
        generate("application-test.yml", spec, context)
        generate("application-prod.yml", spec, context)
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
