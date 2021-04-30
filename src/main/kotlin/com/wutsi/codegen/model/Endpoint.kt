package com.wutsi.codegen.model

data class Endpoint(
    val name: String,
    val path: String,
    val method: String,
    val request: Request? = null,
    val response: Type? = null,
    val parameters: List<EndpointParameter> = emptyList(),
    val securities: List<EndpointSecurity> = emptyList()
)
