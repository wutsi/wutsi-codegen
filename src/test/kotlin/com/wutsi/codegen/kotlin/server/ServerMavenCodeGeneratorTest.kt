package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.helpers.AbstractMustacheCodeGeneratorTest
import com.wutsi.codegen.kotlin.KotlinMapper
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertFalse

internal class ServerMavenCodeGeneratorTest : AbstractMustacheCodeGeneratorTest() {
    override fun createContext() = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    override fun getCodeGenerator(context: Context) = ServerMavenCodeGenerator(KotlinMapper(context))

    @Test
    fun `generate`() {
        val openAPI = createOpenAPI()
        val context = createContext()
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/server/pom.xml", "${context.outputDirectory}/pom.xml")
        assertFalse(File("${context.outputDirectory}/settings.xml").exists())
    }

    @Test
    fun `generate with distribution`() {
        val openAPI = createOpenAPI()
        val context = Context(
            apiName = "Test",
            outputDirectory = "./target/wutsi/codegen/server",
            basePackage = "com.wutsi.test",
            jdkVersion = "1.8",
            githubUser = "foo"
        )
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/server/pom-distribution.xml", "${context.outputDirectory}/pom.xml")
        assertContent("/settings.xml", "${context.outputDirectory}/settings.xml")
    }

    @Test
    fun `generate - do not override`() {
        val openAPI = createOpenAPI()
        val context = createContext()

        val path = "${context.outputDirectory}/pom.xml"
        createFileAndWait(path)

        getCodeGenerator(context).generate(openAPI, context)

        assertFileNotOverwritten(path)
    }
}
