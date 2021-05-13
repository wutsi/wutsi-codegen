package com.wutsi.codegen.editorconfig

import com.wutsi.codegen.Context
import com.wutsi.codegen.helpers.AbstractMustacheCodeGeneratorTest
import org.junit.jupiter.api.Test

internal class EditorConfigCodeGeneratorTest : AbstractMustacheCodeGeneratorTest() {
    override fun createContext() = Context(
        apiName = "test",
        outputDirectory = "./target/wutsi/codegen/editorconfig",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    override fun getCodeGenerator(context: Context) = EditorConfigCodeGenerator()

    @Test
    fun `generate`() {
        val openAPI = createOpenAPI()
        val context = createContext()
        getCodeGenerator(context).generate(openAPI, context)

        assertContent("/.editorconfig", "${context.outputDirectory}/.editorconfig")
    }

    @Test
    fun `generate - do not overwrite`() {
        val openAPI = createOpenAPI()
        val context = createContext()

        val path = "${context.outputDirectory}/.editorconfig"
        createFileAndWait(path)

        getCodeGenerator(context).generate(openAPI, context)

        assertFileNotOverwritten(path)
    }
}
