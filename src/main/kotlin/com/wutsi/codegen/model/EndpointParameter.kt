package com.wutsi.codegen.model

data class EndpointParameter(
    val name: String,
    val field: Field,
    val type: ParameterType
)
