package com.wutsi.codegen

import com.wutsi.codegen.model.Type
import java.io.File

class Context(
    val apiName: String,
    val basePackage: String,
    val outputDirectory: String = ".${File.separator}out",
    val jdkVersion: String = "11",
    val githubUser: String? = null,
    val herokuApp: String? = null
) {
    companion object {
        const val SERVICE_LOGGING = "service:logging"
        const val SERVICE_CACHE = "service:cache"
        const val SERVICE_DATABASE = "service:database"
        const val SERVICE_MESSAGE_QUEUE = "service:message_queue"
    }

    private val services: MutableList<String> = mutableListOf()
    private val typeRegistry = mutableMapOf<String, Type>()

    fun register(ref: String, type: Type) {
        typeRegistry[ref] = type
    }

    fun getType(ref: String): Type? = typeRegistry[ref]

    fun addService(service: String) {
        services.add(service)
    }

    fun hasService(service: String): Boolean = services.contains(service)
}
