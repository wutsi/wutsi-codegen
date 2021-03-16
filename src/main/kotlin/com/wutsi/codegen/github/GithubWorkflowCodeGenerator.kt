package com.wutsi.codegen.github

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractMustacheCodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

class GithubWorkflowCodeGenerator : AbstractMustacheCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        generate("master.yml", openAPI, context)
        generate("pull_request.yml", openAPI, context)
    }

    override fun toMustacheScope(openAPI: OpenAPI, context: Context): Map<String, String> =
        mapOf(
            "jdkVersion" to context.jdkVersion,
            "secrets.GITHUB_TOKEN" to "{{ secrets.GITHUB_TOKEN }}"
        )

    private fun generate(filename: String, openAPI: OpenAPI, context: Context) {
        val file = File(context.outputDirectory + File.separator + ".github" + File.separator + "workflows" + File.separator + filename)
        generate("/.github/workflows/$filename.mustache", file, openAPI, context)
    }
}
