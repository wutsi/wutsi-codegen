package com.wutsi.codegen.model

data class Api(
    val packageName: String,
    val name: String,
    val endpoints: List<Endpoint>,
    val servers: List<Server> = emptyList(),
    val securities: List<Security> = emptyList()
) {
    fun isSecured(): Boolean =
        endpoints.find { it.securities.isNotEmpty() } != null
}
