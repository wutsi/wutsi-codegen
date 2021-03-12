package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SdkPomCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/sdk",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    val codegen = SdkPomCodeGenerator(KotlinMapper(context))

    @Test
    fun `toPom`() {
        val openAPI = createOpenAPI()
        val result = codegen.toPom(openAPI, context)

        assertEquals(4, result.size)
        assertEquals(openAPI.info.version, result["version"])
        assertEquals("test-sdk", result["artifactId"])
        assertEquals(context.basePackage, result["groupId"])
        assertEquals(context.jdkVersion, result["jdkVersion"])
    }

    @Test
    fun generate() {
        val openAPI = createOpenAPI()

        codegen.generate(openAPI, context)

        val file = File("${context.outputDirectory}/pom.xml")
        assertTrue(file.exists())

        val result = file.readText()
        val expected = IOUtils.toString(SdkPomCodeGenerator::class.java.getResourceAsStream("/kotlin/sdk/pom.xml"))
        assertEquals(expected.trimIndent(), result.trimIndent())
    }

    private fun createOpenAPI(): OpenAPI {
        val openAPI = OpenAPI()
        openAPI.info = Info()
        openAPI.info.version = "1.3.7"
        return openAPI
    }
}
