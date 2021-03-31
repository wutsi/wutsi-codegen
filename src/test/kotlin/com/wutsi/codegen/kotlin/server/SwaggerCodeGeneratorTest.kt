package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.FileSystemUtils
import java.io.File
import java.net.URL
import java.nio.file.Files

internal class SwaggerCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        inputUrl = URL("https://petstore.swagger.io/v2/swagger.json")
    )

    val codegen = SwaggerCodeGenerator()

    @BeforeEach
    fun setUp() {
        FileSystemUtils.deleteRecursively(File(context.outputDirectory))
    }

    @Test
    fun generate() {
        codegen.generate(OpenAPI(), context)

        val out = File("${context.outputDirectory}/docs/api")
        assertEquals(9, out.list().size)

        val index = File("${context.outputDirectory}/docs/api/index.html")
        assertTrue(index.exists())

        val content = Files.readString(index.toPath())
        assertTrue(content.contains("./api.yaml"))
    }
}
