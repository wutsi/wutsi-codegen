package com.wutsi.codegen.editconfig

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.CodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import org.apache.commons.io.FileUtils
import java.io.File

class EditorConfigCodeGenerator : CodeGenerator {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val dir = File(context.outputDirectory)
        val input = EditorConfigCodeGenerator::class.java.getResourceAsStream("/.editorconfig")
        val output = File(dir, ".editorconfig")
        if (!output.exists())
            FileUtils.copyToFile(input, output)
    }
}
