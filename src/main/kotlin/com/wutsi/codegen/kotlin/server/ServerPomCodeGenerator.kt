package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.AbstractPomCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.kotlin.sdk.SdkPomCodeGenerator
import com.wutsi.codegen.util.CaseUtil
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

class ServerPomCodeGenerator(mapper: KotlinMapper) : AbstractPomCodeGenerator(mapper) {
    override fun toPom(openAPI: OpenAPI, context: Context) = mapOf(
        "artifactId" to artifactId(context),
        "sdkArtifactId" to SdkPomCodeGenerator(mapper).artifactId(context),
        "groupId" to context.basePackage,
        "jdkVersion" to context.jdkVersion,
        "version" to openAPI.info!!.version
    )

    override fun getTemplatePath(): String = "/kotlin/server/pom.xml.mustache"

    override fun canGenerate(context: Context): Boolean =
        !File(context.outputDirectory, "pom.xml").exists()

    fun artifactId(context: Context): String =
        CaseUtil.toSnakeCase(context.apiName.toLowerCase()) + "-server"
}
