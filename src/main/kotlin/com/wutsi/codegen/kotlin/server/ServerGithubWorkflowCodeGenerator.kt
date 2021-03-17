package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractMustacheCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

class ServerGithubWorkflowCodeGenerator : AbstractMustacheCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        generate("master.yml", openAPI, context)
        generate("pull_request.yml", openAPI, context)
    }

    override fun toMustacheScope(openAPI: OpenAPI, context: Context): Map<String, String?> =
        mapOf(
            "artifactId" to ServerMavenCodeGenerator(KotlinMapper(context)).artifactId(context),
            "version" to openAPI.info?.version,
            "jdkVersion" to context.jdkVersion,
            "secrets.GITHUB_TOKEN" to "{{secrets.GITHUB_TOKEN}}",
            "herokuApp" to context.herokuApp,
            "secrets.HEROKU_API_KEY" to "{{secrets.HEROKU_API_KEY}}"
        )

    private fun generate(filename: String, openAPI: OpenAPI, context: Context) {
        val file = File(context.outputDirectory + File.separator + ".github" + File.separator + "workflows" + File.separator + filename)
        generate("/.github/workflows/$filename.mustache", file, openAPI, context)
    }
}
