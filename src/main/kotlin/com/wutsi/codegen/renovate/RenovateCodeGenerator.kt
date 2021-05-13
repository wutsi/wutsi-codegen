package com.wutsi.codegen.renovate

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.CodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import org.apache.commons.io.FileUtils
import java.io.File

class RenovateCodeGenerator : CodeGenerator {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val dir = File(context.outputDirectory)
        val input = RenovateCodeGenerator::class.java.getResourceAsStream("/renovate.json")
        val output = File(dir, "renovate.json")
        FileUtils.copyToFile(input, output)
    }
}
