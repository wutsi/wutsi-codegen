package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.helpers.AbstractMustacheCodeGeneratorTest
import com.wutsi.codegen.kotlin.KotlinMapper
import org.junit.jupiter.api.Test

internal class SdkMavenCodeGeneratorTest : AbstractMustacheCodeGeneratorTest() {
    override fun createContext() = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/sdk",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8",
        githubProject = "test-sdk",
        githubUser = "foo"
    )

    override fun getCodeGenerator(context: Context) = SdkMavenCodeGenerator(KotlinMapper(context))

    @Test
    fun `generate`() {
        val openAPI = createOpenAPI()
        val context = createContext()
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/sdk/pom.xml", "${context.outputDirectory}/pom.xml")
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
