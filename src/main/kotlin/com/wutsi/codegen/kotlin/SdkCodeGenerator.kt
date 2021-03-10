package com.wutsi.codegen.kotlin

import com.wutsi.codegen.CodeGenerator
import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI

class SdkCodeGenerator(private val mapper: KotlinMapper) : CodeGenerator {
    override fun generate(openAPI: OpenAPI, context: Context) {
        SdkModelCodeGenerator(mapper).generate(openAPI, context)
        SdkApiCodeGenerator(mapper).generate(openAPI, context)
    }
}
