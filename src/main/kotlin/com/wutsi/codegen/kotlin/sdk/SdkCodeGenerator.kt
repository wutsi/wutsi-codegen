package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.CodeGenerator
import com.wutsi.codegen.github.GitCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI

class SdkCodeGenerator(
    private val mapper: KotlinMapper,
    val generators: List<CodeGenerator> = listOf(
        SdkDtoCodeGenerator(mapper),
        SdkApiCodeGenerator(mapper),
        SdkMavenCodeGenerator(mapper),
        GitCodeGenerator(),
        SdkGithubActionsCodeGenerator(),
        SdkReadmeCodeGenerator(),
        SdkEnvironmentGenerator(),
        SdkApiBuilderCodeGenerator(mapper)
    )
) : CodeGenerator {
    override fun generate(openAPI: OpenAPI, context: Context) {
        generators.forEach { it.generate(openAPI, context) }
    }
}
