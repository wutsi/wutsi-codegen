package com.wutsi.codegen.core.openapi

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser

class DefaultOpenAPILoader(private val parser: OpenAPIV3Parser = OpenAPIV3Parser()) : OpenAPILoader {
    override fun load(url: String): OpenAPI =
        parser.read(url)
}
