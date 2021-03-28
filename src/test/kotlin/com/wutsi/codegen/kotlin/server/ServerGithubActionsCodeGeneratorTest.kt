package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.helpers.AbstractMustacheCodeGeneratorTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class ServerGithubActionsCodeGeneratorTest : AbstractMustacheCodeGeneratorTest() {
    override fun createContext() = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/github",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    override fun getCodeGenerator(context: Context) = ServerGithubActionsCodeGenerator()

    @Test
    fun `generate`() {
        val openAPI = createOpenAPI()
        val context = createContext()
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/server/.github/workflows/master.yml", "${context.outputDirectory}/.github/workflows/master.yml")
        assertContent("/kotlin/server/.github/workflows/pull_request.yml", "${context.outputDirectory}/.github/workflows/pull_request.yml")
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
        context.addService(Context.SERVICE_CACHE)
        context.addService(Context.SERVICE_DATABASE)
        context.addService(Context.SERVICE_LOGGING)
        context.addService(Context.SERVICE_MQUEUE)
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/server/heroku/.github/workflows/master.yml", "${context.outputDirectory}/.github/workflows/master.yml")
        assertContent("/kotlin/server/heroku/.github/workflows/pull_request.yml", "${context.outputDirectory}/.github/workflows/pull_request.yml")
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
        assertContent("/kotlin/server/.github/workflows/$name", "${context.outputDirectory}/.github/workflows/$name")
    }
}
