package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.helpers.AbstractMustacheCodeGeneratorTest
import com.wutsi.codegen.kotlin.KotlinMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class ServerConfigCodeGeneratorTest : AbstractMustacheCodeGeneratorTest() {
    override fun createContext() = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    override fun getCodeGenerator(context: Context) = ServerConfigCodeGenerator(KotlinMapper(context))

    @Test
    fun `generate`() {
        val openAPI = createOpenAPI()
        val context = createContext()
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/server/application.yml", "${context.outputDirectory}/src/main/resources/application.yml")
        assertContent("/kotlin/server/application-test.yml", "${context.outputDirectory}/src/main/resources/application-test.yml")
        assertContent("/kotlin/server/application-prod.yml", "${context.outputDirectory}/src/main/resources/application-prod.yml")
    }

    @Test
    fun `generate with database`() {
        val openAPI = createOpenAPI()
        val context = createContext()
        context.addService(Context.SERVICE_DATABASE)
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/kotlin/server/database/application.yml", "${context.outputDirectory}/src/main/resources/application.yml")
        assertContent("/kotlin/server/database/application-test.yml", "${context.outputDirectory}/src/main/resources/application-test.yml")
        assertContent("/kotlin/server/database/application-prod.yml", "${context.outputDirectory}/src/main/resources/application-prod.yml")
    }

    @ParameterizedTest
    @ValueSource(strings = ["application.yml", "application-test.yml", "application-prod.yml"])
    fun `generate - do not overwrite`(name: String) {
        val openAPI = createOpenAPI()
        val context = createContext()

        val path = "${context.outputDirectory}/kotlin/server/$name"
        createFileAndWait(path)

        getCodeGenerator(context).generate(openAPI, context)

        assertFileNotOverwritten(path)
    }
}
