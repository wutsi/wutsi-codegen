package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.github.AbstractGithubActionsCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI

class SdkGithubActionsCodeGenerator : AbstractGithubActionsCodeGenerator() {
    override fun getInputFilePath(filename: String): String =
        "/kotlin/sdk/.github/workflows/$filename.mustache"

    override fun toMustacheScope(openAPI: OpenAPI, context: Context) = mapOf(
        "artifactId" to SdkMavenCodeGenerator(KotlinMapper(context)).artifactId(context),
        "version" to openAPI.info?.version,
        "jdkVersion" to context.jdkVersion,
        "secrets.GITHUB_TOKEN" to "{{secrets.GITHUB_TOKEN}}"
    )
}
