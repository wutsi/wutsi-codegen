package com.wutsi.codegen.github

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractStaticCodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

class GitCodeGenerator : AbstractStaticCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val file = File(context.outputDirectory, ".gitignore")
        generate("/.gitignore", file, openAPI, context)
    }
}
