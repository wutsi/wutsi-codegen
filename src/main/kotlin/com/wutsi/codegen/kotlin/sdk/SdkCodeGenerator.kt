package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.generator.CodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI

class SdkCodeGenerator(private val mapper: KotlinMapper) : CodeGenerator {
    override fun generate(openAPI: OpenAPI, context: Context) {
        SdkModelCodeGenerator(mapper).generate(openAPI, context)
        SdkApiCodeGenerator(mapper).generate(openAPI, context)
        SdkPomCodeGenerator(mapper).generate(openAPI, context)
    }
}
