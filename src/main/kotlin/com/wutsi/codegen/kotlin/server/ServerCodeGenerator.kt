package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.CodeGenerator
import com.wutsi.codegen.editorconfig.EditorConfigCodeGenerator
import com.wutsi.codegen.github.GitCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.renovate.RenovateCodeGenerator
import io.swagger.v3.oas.models.OpenAPI

class ServerCodeGenerator(
    private val mapper: KotlinMapper,
    val generators: List<CodeGenerator> = listOf(
        ServerDtoCodeGenerator(mapper),
        ServerDelegateCodeGenerator(mapper),
        ServerControllerCodeGenerator(mapper),
        ServerMavenCodeGenerator(mapper),
        ServerLauncherCodeGenerator(),
        ServerConfigCodeGenerator(),
        ServerHerokuCodeGenerator(mapper),
        EditorConfigCodeGenerator(),
        ServerGithubActionsCodeGenerator(),
        GitCodeGenerator(),
        SwaggerCodeGenerator(),
        ServerReadmeCodeGenerator(),
        DatabaseCodeGenerator(),
        SecurityCodeGenerator(),
        RenovateCodeGenerator()
    )
) : CodeGenerator {

    override fun generate(openAPI: OpenAPI, context: Context) {
        generators.forEach { it.generate(openAPI, context) }
    }
}
