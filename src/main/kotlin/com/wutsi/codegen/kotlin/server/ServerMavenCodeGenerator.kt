package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractMavenCodeGenerator
import com.wutsi.codegen.core.util.CaseUtil
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.kotlin.sdk.SdkMavenCodeGenerator
import io.swagger.v3.oas.models.OpenAPI

class ServerMavenCodeGenerator(private val mapper: KotlinMapper) : AbstractMavenCodeGenerator() {
    override fun getTemplatePath() = "/kotlin/server/pom.xml.mustache"

    override fun toMustacheScope(openAPI: OpenAPI, context: Context): Map<String, Any?> {
        val api = mapper.toAPI(openAPI)
        return mapOf(
            "artifactId" to artifactId(context),
            "sdkArtifactId" to SdkMavenCodeGenerator(mapper).artifactId(context),
            "groupId" to context.basePackage,
            "jdkVersion" to context.jdkVersion,
            "version" to openAPI.info?.version,
            "githubUser" to context.githubUser,
            "githubProject" to context.githubProject,
            "services" to toServices(context),
            "security" to api.isSecured()
        )
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
        if (context.hasService(Context.SERVICE_MQUEUE)) {
            result["mqueue"] = true
        }
        return result
    }

    fun artifactId(context: Context): String =
        CaseUtil.toSnakeCase(context.apiName.toLowerCase()) + "-server"
}
