package com.wutsi.codegen.github

import com.wutsi.codegen.Context
import com.wutsi.codegen.helpers.AbstractMustacheCodeGeneratorTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class GithubWorkflowCodeGeneratorTest : AbstractMustacheCodeGeneratorTest() {
    override fun createContext() = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/github",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    override fun getCodeGenerator(context: Context) = GithubWorkflowCodeGenerator()

    @Test
    fun `generate`() {
        val openAPI = createOpenAPI()
        val context = createContext()
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/.github/workflows/master.yml", "${context.outputDirectory}/.github/workflows/master.yml")
        assertContent("/.github/workflows/pull_request.yml", "${context.outputDirectory}/.github/workflows/pull_request.yml")
    }

    @Test
    fun `generate with Heroku`() {
        val openAPI = createOpenAPI()
        val context = Context(
            apiName = "Test",
            outputDirectory = "./target/wutsi/codegen/github",
            basePackage = "com.wutsi.test",
            jdkVersion = "1.8",
            herokuApp = "foo-app"
        )
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/.github/workflows/master-heroku.yml", "${context.outputDirectory}/.github/workflows/master.yml")
        assertContent("/.github/workflows/pull_request.yml", "${context.outputDirectory}/.github/workflows/pull_request.yml")
    }

    @ParameterizedTest
    @ValueSource(strings = ["master.yml", "pull_request.yml"])
    fun `generate - do not overwrite`(name: String) {
        val openAPI = createOpenAPI()
        val context = createContext()

        val path = "${context.outputDirectory}/.github/workflows/$name"
        createFileAndWait(path)

        getCodeGenerator(context).generate(openAPI, context)

        assertFileNotOverwritten(path)
    }
}
