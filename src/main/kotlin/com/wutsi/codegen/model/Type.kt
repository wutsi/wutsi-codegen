package com.wutsi.codegen.model

data class Type(
    val name: String,
    val packageName: String,
    val fields: List<Field> = mutableListOf()
)
