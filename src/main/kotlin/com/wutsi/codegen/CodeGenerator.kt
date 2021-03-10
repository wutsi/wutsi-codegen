package com.wutsi.codegen

import io.swagger.v3.oas.models.OpenAPI

interface CodeGenerator {
    fun generate(spec: OpenAPI, context: Context)
}
