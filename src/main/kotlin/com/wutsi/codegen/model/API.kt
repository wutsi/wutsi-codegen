package com.wutsi.codegen.model

data class API(
    val packageName: String,
    val name: String,
    val endpoints: List<Endpoint>
)
