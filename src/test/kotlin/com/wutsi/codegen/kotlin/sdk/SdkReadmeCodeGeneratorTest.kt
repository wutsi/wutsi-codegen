package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.helpers.AbstractMustacheCodeGeneratorTest
import org.junit.jupiter.api.Test

internal class SdkReadmeCodeGeneratorTest : AbstractMustacheCodeGeneratorTest() {

    override fun createContext() = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/sdk",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8",
        githubUser = "foo",
        githubProject = "test-service"
    )

    override fun getCodeGenerator(context: Context) = SdkReadmeCodeGenerator()

    @Test
    fun `generate`() {
        val openAPI = createOpenAPI()
        val context = createContext()
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/sdk/README.md", "${context.outputDirectory}/README.md")
    }
}
