package com.wutsi.codegen

import io.swagger.v3.oas.models.OpenAPI

interface OpenAPILoader {
    fun load(url: String): OpenAPI
}
