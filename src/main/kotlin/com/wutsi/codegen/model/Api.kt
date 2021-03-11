package com.wutsi.codegen.model

data class Api(
    val packageName: String,
    val name: String,
    val endpoints: List<Endpoint>
)
