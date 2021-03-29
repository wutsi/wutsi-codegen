package com.wutsi.codegen.core.generator

import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI

interface CodeGenerator {
    fun generate(openAPI: OpenAPI, context: Context)
}
