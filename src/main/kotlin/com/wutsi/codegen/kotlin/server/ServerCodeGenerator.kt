package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.CodeGenerator
import com.wutsi.codegen.editorconfig.EditorConfigCodeGenerator
import com.wutsi.codegen.github.GitCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI

class ServerCodeGenerator(
    private val mapper: KotlinMapper,
    val generators: List<CodeGenerator> = listOf(
        ServerModelCodeGenerator(mapper),
        ServerDelegateCodeGenerator(mapper),
        ServerControllerCodeGenerator(mapper),
        ServerMavenCodeGenerator(mapper),
        ServerLauncherCodeGenerator(),
        ServerConfigCodeGenerator(mapper),
        ServerHerokuCodeGenerator(mapper),
        EditorConfigCodeGenerator(),
        ServerGithubActionsCodeGenerator(),
        GitCodeGenerator(),
        CacheCodeGenerator(),
        MQueueCodeGenerator()
    )
) : CodeGenerator {

    override fun generate(openAPI: OpenAPI, context: Context) {
        generators.forEach { it.generate(openAPI, context) }
    }
}
