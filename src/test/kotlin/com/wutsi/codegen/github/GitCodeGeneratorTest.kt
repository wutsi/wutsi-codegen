package com.wutsi.codegen.github

import com.wutsi.codegen.Context
import com.wutsi.codegen.helpers.AbstractMustacheCodeGeneratorTest
import org.junit.jupiter.api.Test

internal class GitCodeGeneratorTest : AbstractMustacheCodeGeneratorTest() {
    override fun createContext() = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/github",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    override fun getCodeGenerator(context: Context) = GitCodeGenerator()

    @Test
    fun `generate`() {
        val openAPI = createOpenAPI()
        val context = createContext()
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/.gitignore", "${context.outputDirectory}/.gitignore")
    }

    @Test
    fun `generate - overwrite`() {
        val openAPI = createOpenAPI()
        val context = createContext()

        val path = "${context.outputDirectory}/.gitignore"
        createFileAndWait(path)

        getCodeGenerator(context).generate(openAPI, context)

        assertFileOverwritten(path)
    }
}
