package com.wutsi.codegen

import com.wutsi.codegen.model.Type
import java.io.File

class Context(
    val apiName: String,
    val basePackage: String,
    val outputDirectory: String = ".${File.separator}out",
    val artifactId: String? = null,
    val groupId: String? = null,
    val jdkVersion: String = "11"
) {
    private val typeRegistry = mutableMapOf<String, Type>()

    fun register(ref: String, type: Type) {
        typeRegistry[ref] = type
    }

    fun getType(ref: String): Type? = typeRegistry[ref]
}
