package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.helpers.AbstractMustacheCodeGeneratorTest
import com.wutsi.codegen.kotlin.KotlinMapper
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.io.File

internal class ServerHerokuCodeGeneratorTest : AbstractMustacheCodeGeneratorTest() {
    override fun createContext() = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8",
        herokuApp = "test-app"
    )

    override fun getCodeGenerator(context: Context) = ServerHerokuCodeGenerator(KotlinMapper(context))

    @Test
    fun `generate`() {
        val openAPI = createOpenAPI()
        val context = createContext()

        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/server/heroku/Procfile", "${context.outputDirectory}/Procfile")
        assertContent("/kotlin/server/heroku/system.properties", "${context.outputDirectory}/system.properties")
    }

    @Test
    fun `generate - no heroku-app`() {
        val openAPI = createOpenAPI()
        val context = Context(
            apiName = "Test",
            outputDirectory = "./target/wutsi/codegen/server",
            basePackage = "com.wutsi.test",
            jdkVersion = "1.8",
            herokuApp = null
        )
        getCodeGenerator(context).generate(openAPI, context)

        assertFalse(File("${context.outputDirectory}/Procfile").exists())
    }

    @Test
    fun `generate - overwrite`() {
        val openAPI = createOpenAPI()
        val context = createContext()

        val path = "${context.outputDirectory}/Procfile"
        createFileAndWait(path)

        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/server/heroku/Procfile", "${context.outputDirectory}/Procfile")
    }
}
