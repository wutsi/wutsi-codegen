package com.wutsi.codegen.model

data class Request(
    val required: Boolean,
    val contentType: String,
    val type: Type
)
