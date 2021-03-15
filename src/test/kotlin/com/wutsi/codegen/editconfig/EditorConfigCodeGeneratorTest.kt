package com.wutsi.codegen.editconfig

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.sdk.SdkPomCodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import kotlin.test.assertTrue

internal class EditorConfigCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/editorconfig",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    val codegen = EditorConfigCodeGenerator()

    @Test
    fun `generate`() {
        val openAPI = OpenAPI()

        codegen.generate(openAPI, context)

        val file = File("${context.outputDirectory}/.editorconfig")
        kotlin.test.assertTrue(file.exists())

        val result = file.readText()
        val expected = IOUtils.toString(SdkPomCodeGenerator::class.java.getResourceAsStream("/.editorconfig"), "utf-8")
        kotlin.test.assertEquals(expected.trimIndent(), result.trimIndent())
    }

    @Test
    fun `generate - do not overwrite`() {
        val path = "${context.outputDirectory}/.editorconfig"
        File(path).parentFile.mkdirs()
        Files.write(
            File(path).toPath(),
            "xxx".toByteArray()
        )

        val delay = 5000L
        Thread.sleep(delay)
        codegen.generate(
            openAPI = OpenAPI(),
            context = context
        )

        val file = File(path)
        assertTrue(System.currentTimeMillis() - file.lastModified() >= delay)
    }
}
