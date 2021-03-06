package com.wutsi.codegen

import com.wutsi.codegen.model.Type
import java.io.File
import java.net.URL

open class Context(
    val apiName: String,
    val basePackage: String,
    val outputDirectory: String = ".${File.separator}out",
    val jdkVersion: String = "11",
    val githubUser: String? = null,
    val githubProject: String? = null,
    val herokuApp: String? = null,
    val inputUrl: URL? = null,
) {
    companion object {
        const val SERVICE_LOGGING = "service:logging"
        const val SERVICE_CACHE = "service:cache"
        const val SERVICE_DATABASE = "service:database"
        const val SERVICE_MQUEUE = "service:mqueue"
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
