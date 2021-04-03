package com.wutsi.codegen.github

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractMustacheCodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

abstract class AbstractGithubActionsCodeGenerator : AbstractMustacheCodeGenerator() {
    abstract fun getInputFilePath(filename: String): String

    override fun generate(openAPI: OpenAPI, context: Context) {
        generate("master.yml", openAPI, context)
        generate("pull_request.yml", openAPI, context)
    }

    protected fun generate(filename: String, openAPI: OpenAPI, context: Context) {
        val file = File(context.outputDirectory + File.separator + ".github" + File.separator + "workflows" + File.separator + filename)
        generate(getInputFilePath(filename), file, openAPI, context)
    }
}
