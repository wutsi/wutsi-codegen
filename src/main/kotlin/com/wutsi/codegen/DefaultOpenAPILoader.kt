package com.wutsi.codegen

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser

class DefaultOpenAPILoader : OpenAPILoader {
    override fun load(url: String): OpenAPI =
        OpenAPIV3Parser().read("https://wutsi-openapi.s3.amazonaws.com/like_api.yaml")
}
