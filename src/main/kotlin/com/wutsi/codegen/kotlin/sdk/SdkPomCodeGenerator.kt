package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractPomCodeGenerator
import com.wutsi.codegen.core.util.CaseUtil
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI

class SdkPomCodeGenerator(private val mapper: KotlinMapper) : AbstractPomCodeGenerator() {
    override fun getTemplatePath() = "/kotlin/sdk/pom.xml.mustache"

    override fun toMustacheScope(openAPI: OpenAPI, context: Context) = mapOf(
        "artifactId" to artifactId(context),
        "groupId" to context.basePackage,
        "jdkVersion" to context.jdkVersion,
        "version" to openAPI.info?.version,
        "githubUser" to context.githubUser
    )

    fun artifactId(context: Context): String =
        CaseUtil.toSnakeCase(context.apiName.toLowerCase()) + "-sdk"
}
