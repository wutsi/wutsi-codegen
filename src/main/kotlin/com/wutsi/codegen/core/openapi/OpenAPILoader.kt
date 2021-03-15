package com.wutsi.codegen.core.openapi

import io.swagger.v3.oas.models.OpenAPI

interface OpenAPILoader {
    fun load(url: String): OpenAPI
}
