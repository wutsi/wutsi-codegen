package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.helpers.AbstractMustacheCodeGeneratorTest
import org.junit.jupiter.api.Test

internal class ServerReadmeCodeGeneratorTest : AbstractMustacheCodeGeneratorTest() {

    override fun createContext() = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8",
        githubUser = "foo",
        githubProject = "test-service"
    )

    override fun getCodeGenerator(context: Context) = ServerReadmeCodeGenerator()

    @Test
    fun `generate`() {
        val openAPI = createOpenAPI()
        val context = createContext()
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/server/README.md", "${context.outputDirectory}/README.md")
    }

    @Test
    fun `generate with database configuration `() {
        val openAPI = createOpenAPI()
        val context = createContext()
        context.addService(Context.SERVICE_DATABASE)
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/server/database/README.md", "${context.outputDirectory}/README.md")
    }

    @Test
    fun `generate with cache configuration `() {
        val openAPI = createOpenAPI()
        val context = createContext()
        context.addService(Context.SERVICE_CACHE)
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/server/cache/README.md", "${context.outputDirectory}/README.md")
    }

    @Test
    fun `generate with mqueue configuration `() {
        val openAPI = createOpenAPI()
        val context = createContext()
        context.addService(Context.SERVICE_MQUEUE)
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/server/mqueue/README.md", "${context.outputDirectory}/README.md")
    }

    @Test
    fun `generate - override`() {
        val openAPI = createOpenAPI()
        val context = createContext()

        val path = "${context.outputDirectory}/README.md"
        createFileAndWait(path)

        getCodeGenerator(context).generate(openAPI, context)

        assertFileOverwritten(path)
    }
}
