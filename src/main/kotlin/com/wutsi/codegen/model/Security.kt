package com.wutsi.codegen.model

data class Security(
    val name: String,
    val type: SecurityType = SecurityType.INVALID,
    val location: SecurityLocation = SecurityLocation.INVALID
)
