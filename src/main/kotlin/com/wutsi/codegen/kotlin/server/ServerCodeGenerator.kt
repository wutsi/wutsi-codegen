package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.generator.CodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.kotlin.sdk.SdkModelCodeGenerator
import io.swagger.v3.oas.models.OpenAPI

class ServerCodeGenerator(private val mapper: KotlinMapper) : CodeGenerator {
    override fun generate(openAPI: OpenAPI, context: Context) {
        SdkModelCodeGenerator(mapper).generate(openAPI, context)
        ServerDelegateCodeGenerator(mapper).generate(openAPI, context)
        ServerControllerCodeGenerator(mapper).generate(openAPI, context)
        ServerPomCodeGenerator(mapper).generate(openAPI, context)
        ServerLauncherCodeGenerator().generate(openAPI, context)
    }
}
