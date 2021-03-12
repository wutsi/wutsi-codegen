package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.FileSystemUtils
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ServerPomCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    val codegen = ServerPomCodeGenerator(KotlinMapper(context))

    @BeforeEach
    fun setUp() {
        FileSystemUtils.deleteRecursively(File(context.outputDirectory))
    }

    @Test
    fun generate() {
        val openAPI = createOpenAPI()

        codegen.generate(openAPI, context)

        val file = File("${context.outputDirectory}/pom.xml")
        assertTrue(file.exists())

        val result = file.readText()
        val expected = IOUtils.toString(ServerPomCodeGenerator::class.java.getResourceAsStream("/kotlin/server/pom.xml"))
        assertEquals(expected.trimIndent(), result.trimIndent())
    }

    private fun createOpenAPI(): OpenAPI {
        val openAPI = OpenAPI()
        openAPI.info = Info()
        openAPI.info.version = "1.3.7"
        return openAPI
    }
}