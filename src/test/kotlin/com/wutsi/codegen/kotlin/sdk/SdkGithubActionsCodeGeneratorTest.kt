package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.helpers.AbstractMustacheCodeGeneratorTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class SdkGithubActionsCodeGeneratorTest : AbstractMustacheCodeGeneratorTest() {
    override fun createContext() = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/sdk",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    override fun getCodeGenerator(context: Context) = SdkGithubActionsCodeGenerator()

    @Test
    fun `generate`() {
        val openAPI = createOpenAPI()
        val context = createContext()
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/sdk/.github/workflows/master.yml", "${context.outputDirectory}/.github/workflows/master.yml")
        assertContent("/kotlin/sdk/.github/workflows/pull_request.yml", "${context.outputDirectory}/.github/workflows/pull_request.yml")
    }

    @ParameterizedTest
    @ValueSource(strings = ["master.yml", "pull_request.yml"])
    fun `generate - overwrite`(name: String) {
        val openAPI = createOpenAPI()
        val context = createContext()

        val path = "${context.outputDirectory}/.github/workflows/$name"
        createFileAndWait(path)

        getCodeGenerator(context).generate(openAPI, context)

        assertFileOverwritten(path)
        assertContent("/kotlin/sdk/.github/workflows/$name", "${context.outputDirectory}/.github/workflows/$name")
    }
}
