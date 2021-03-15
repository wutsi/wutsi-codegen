package com.wutsi.codegen.openapi

import io.swagger.v3.oas.models.OpenAPI

interface OpenAPILoader {
    fun load(url: String): OpenAPI
}
