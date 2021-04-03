package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.util.CaseUtil
import com.wutsi.codegen.github.AbstractGithubActionsCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI

class ServerGithubActionsCodeGenerator : AbstractGithubActionsCodeGenerator() {
    override fun getInputFilePath(filename: String): String =
        "/kotlin/server/.github/workflows/$filename.mustache"

    override fun toMustacheScope(openAPI: OpenAPI, context: Context) = mapOf(
        "artifactId" to ServerMavenCodeGenerator(KotlinMapper(context)).artifactId(context),
        "version" to openAPI.info?.version,
        "jdkVersion" to context.jdkVersion,
        "secrets.GITHUB_TOKEN" to "{{secrets.GITHUB_TOKEN}}",

        "secrets.HEROKU_API_KEY" to "{{secrets.HEROKU_API_KEY}}",
        "secrets.HEROKU_API_KEY_TEST" to "{{secrets.HEROKU_API_KEY_TEST}}",
        "secrets.HEROKU_API_KEY_PROD" to "{{secrets.HEROKU_API_KEY_PROD}}",
        "herokuApp" to context.herokuApp,
        "herokuAddons" to toAddOns(context),
        "services" to toServices(context)
    )

    private fun toAddOns(context: Context): List<Map<String, String>> {
        val addons = mutableListOf<Map<String, String>>()
        if (context.hasService(Context.SERVICE_LOGGING))
            addons.add(mapOf("addonName" to "papertrail"))
        if (context.hasService(Context.SERVICE_CACHE))
            addons.add(mapOf("addonName" to "memcachier"))
        if (context.hasService(Context.SERVICE_DATABASE))
            addons.add(mapOf("addonName" to "heroku-postgresql"))
//        Never add MQUEUE.. Queues are shared
//        if (context.hasService(Context.SERVICE_MQUEUE))
//            addons.add(mapOf("addonName" to "cloudamqp"))
        return addons
    }

    private fun toServices(context: Context): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        if (context.hasService(Context.SERVICE_DATABASE)) {
            result["database"] = true
            result["databaseName"] = CaseUtil.toSnakeCase(context.apiName).toLowerCase()
        }
        if (context.hasService(Context.SERVICE_CACHE)) {
            result["cache"] = true
        }
        return result
    }
}
