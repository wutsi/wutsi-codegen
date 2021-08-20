package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractMustacheCodeGenerator
import com.wutsi.codegen.core.util.CaseUtil
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Api
import com.wutsi.codegen.model.Endpoint
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

class ServerConfigCodeGenerator : AbstractMustacheCodeGenerator() {
    override fun toMustacheScope(openAPI: OpenAPI, context: Context): Map<String, Any?> {
        val api = KotlinMapper(context).toAPI(openAPI)
        return mapOf(
            "services" to toServices(context),
            "security" to toSecurity(api),
            "basePackage" to context.basePackage,
            "name" to context.apiName.toLowerCase(),
            "securedEndpoints" to toSecureEndpoints(api)
        )
    }

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

    private fun toSecurity(api: Api): Map<String, Any?>? =
        if (!api.isSecured())
            null
        else
            mapOf(
                "endpoints" to toSecureEndpoints(api)
            )

    private fun toSecureEndpoints(api: Api): String? {

        val items = api.endpoints
            .filter { it.isSecured() }
            .map { "\"${it.method} ${toAntPath(it)}\"" }
            .toSet()
        return items.joinToString(separator = ",")
    }

    private fun toAntPath(endpoint: Endpoint): String {
        val tokens = endpoint.path.split("/")
        if (tokens.isEmpty())
            return endpoint.path
        else {
            val xtokens = tokens
                .filter { it.isNotEmpty() }
                .map { if (it.startsWith("{")) "*" else it }
            return "/" + xtokens.joinToString(separator = "/")
        }

        endpoint.path.replace("(\\{.+})".toRegex(), "*")
    }
}
