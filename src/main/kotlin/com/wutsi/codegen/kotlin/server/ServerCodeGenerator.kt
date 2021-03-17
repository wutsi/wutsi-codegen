package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.CodeGenerator
import com.wutsi.codegen.editconfig.EditorConfigCodeGenerator
import com.wutsi.codegen.github.GitIgnoreCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.kotlin.sdk.SdkModelCodeGenerator
import io.swagger.v3.oas.models.OpenAPI

class ServerCodeGenerator(private val mapper: KotlinMapper) : CodeGenerator {
    override fun generate(openAPI: OpenAPI, context: Context) {
        SdkModelCodeGenerator(mapper).generate(openAPI, context)

        ServerDelegateCodeGenerator(mapper).generate(openAPI, context)
        ServerControllerCodeGenerator(mapper).generate(openAPI, context)
        ServerMavenCodeGenerator(mapper).generate(openAPI, context)
        ServerLauncherCodeGenerator().generate(openAPI, context)
        ServerConfigCodeGenerator(mapper).generate(openAPI, context)
        ServerHerokuCodeGenerator(mapper).generate(openAPI, context)

        EditorConfigCodeGenerator().generate(openAPI, context)
        ServerGithubWorkflowCodeGenerator().generate(openAPI, context)
        GitIgnoreCodeGenerator().generate(openAPI, context)
    }
}
