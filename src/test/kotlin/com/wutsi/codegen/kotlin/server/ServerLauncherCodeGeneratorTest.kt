package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.sdk.SdkCodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
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
                import org.springframework.scheduling.`annotation`.EnableAsync

                @SpringBootApplication
                @EnableAsync
                public class Application

                public fun main(vararg args: String): Unit {
                  org.springframework.boot.runApplication<Application>(*args)
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }

    @Test
    fun `generate with security`() {
        val openAPI = createOpenAPI(true)
        context.addService(Context.SERVICE_DATABASE)

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
                import org.springframework.scheduling.`annotation`.EnableAsync
                import org.springframework.security.config.`annotation`.method.configuration.EnableGlobalMethodSecurity
                import org.springframework.security.config.`annotation`.web.configuration.EnableWebSecurity
                import org.springframework.transaction.`annotation`.EnableTransactionManagement

                @SpringBootApplication
                @EnableAsync
                @EnableTransactionManagement
                @EnableWebSecurity
                @EnableGlobalMethodSecurity(prePostEnabled = true)
                public class Application

                public fun main(vararg args: String): Unit {
                  org.springframework.boot.runApplication<Application>(*args)
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }

    @Test
    fun `generate with database`() {
        val openAPI = createOpenAPI()
        context.addService(Context.SERVICE_DATABASE)

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
                import org.springframework.scheduling.`annotation`.EnableAsync
                import org.springframework.transaction.`annotation`.EnableTransactionManagement

                @SpringBootApplication
                @EnableAsync
                @EnableTransactionManagement
                public class Application

                public fun main(vararg args: String): Unit {
                  org.springframework.boot.runApplication<Application>(*args)
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }

    @Test
    fun `generate with cache`() {
        val openAPI = createOpenAPI()
        context.addService(Context.SERVICE_CACHE)

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
                import org.springframework.cache.`annotation`.EnableCaching
                import org.springframework.scheduling.`annotation`.EnableAsync

                @SpringBootApplication
                @EnableAsync
                @EnableCaching
                public class Application

                public fun main(vararg args: String): Unit {
                  org.springframework.boot.runApplication<Application>(*args)
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }

    @Test
    fun `generate with mqueue`() {
        val openAPI = createOpenAPI()
        context.addService(Context.SERVICE_MQUEUE)

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
                import org.springframework.scheduling.`annotation`.EnableAsync
                import org.springframework.scheduling.`annotation`.EnableScheduling

                @SpringBootApplication
                @EnableAsync
                @EnableScheduling
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
        assertTrue(System.currentTimeMillis() - file.lastModified() >= delay)
    }

    private fun createOpenAPI(withSecurity: Boolean = false): OpenAPI {
        if (withSecurity) {
            val yaml = IOUtils.toString(SdkCodeGenerator::class.java.getResourceAsStream("/api.yaml"), "utf-8")
            return OpenAPIV3Parser().readContents(yaml).openAPI
        } else {
            val openAPI = OpenAPI()
            openAPI.info = Info()
            openAPI.info.version = "1.3.7"
            return openAPI
        }
    }
}
