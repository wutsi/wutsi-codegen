package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.FileSystemUtils
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ServerLauncherCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8",
        githubUser = null
    )

    val codegen = ServerLauncherCodeGenerator()

    @BeforeEach
    fun setUp() {
        FileSystemUtils.deleteRecursively(File(context.outputDirectory))
    }

    @Test
    fun `generate`() {
        val openAPI = createOpenAPI()

        codegen.generate(openAPI, context)

        // Launcher
        val file = File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/Application.kt")
        assertTrue(file.exists())

        val text = file.readText()
        assertEquals(
            """
                package com.wutsi.test

                import kotlin.String
                import kotlin.Unit
                import org.springframework.boot.autoconfigure.SpringBootApplication

                @SpringBootApplication
                public class Application

                public fun main(vararg args: String): Unit {
                  org.springframework.boot.runApplication<Application>(*args)
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }

    @Test
    fun `generate - do not overwrite`() {
        val openAPI = createOpenAPI()

        var path = "${context.outputDirectory}/src/main/kotlin/com/wutsi/test/Application.kt"
        File(path).parentFile.mkdirs()
        Files.write(
            File(path).toPath(),
            "xxx".toByteArray()
        )

        val delay = 5000L
        Thread.sleep(delay)
        codegen.generate(openAPI, context)

        val file = File(path)
        kotlin.test.assertTrue(System.currentTimeMillis() - file.lastModified() >= delay)
    }

    private fun createOpenAPI(): OpenAPI {
        val openAPI = OpenAPI()
        openAPI.info = Info()
        openAPI.info.version = "1.3.7"
        return openAPI
    }
}
