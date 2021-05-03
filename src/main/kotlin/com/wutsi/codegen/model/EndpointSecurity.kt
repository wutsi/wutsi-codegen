package com.wutsi.codegen.model

data class EndpointSecurity(
    val name: String,
    val scopes: List<String> = emptyList()
)
