package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractMustacheCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

class ServerHerokuCodeGenerator(private val mapper: KotlinMapper) : AbstractMustacheCodeGenerator() {
    override fun toMustacheScope(openAPI: OpenAPI, context: Context) = mapOf(
        "artifactId" to ServerMavenCodeGenerator(mapper).artifactId(context),
        "jdkVersion" to context.jdkVersion,
        "version" to openAPI.info?.version,
        "githubUser" to context.githubUser,
        "herokuApp" to context.herokuApp,
        "description" to openAPI.info.description
    )

    override fun generate(openAPI: OpenAPI, context: Context) {
        context.herokuApp ?: return

        generate(
            inputPath = "/kotlin/server/heroku/Procfile.mustache",
            outputFile = File(context.outputDirectory, "Procfile"),
            openAPI = openAPI,
            context = context
        )
        generate(
            inputPath = "/kotlin/server/heroku/app.json.mustache",
            outputFile = File(context.outputDirectory, "app.json"),
            openAPI = openAPI,
            context = context
        )
        generate(
            inputPath = "/kotlin/server/heroku/system.properties.mustache",
            outputFile = File(context.outputDirectory, "system.properties"),
            openAPI = openAPI,
            context = context
        )
    }
}
