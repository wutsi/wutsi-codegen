package com.wutsi.codegen

data class Context(
    val apiName: String,
    val basePackage: String,
    val outputDirectory: String = "./out",
    val artifactId: String? = null,
    val groupId: String? = null,
    val jdkVersion: String = "11"
)
