package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.FileSystemUtils
import java.io.File
import java.nio.file.Files

internal class DatabaseCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    val codegen = DatabaseCodeGenerator()

    @BeforeEach
    fun setUp() {
        FileSystemUtils.deleteRecursively(File(context.outputDirectory))
    }

    @Test
    fun `generate flyway files`() {
        val openAPI = OpenAPI()
        context.addService(Context.SERVICE_DATABASE)

        codegen.generate(openAPI, context)

        assertTrue(File("${context.outputDirectory}/src/main/resources/db/migration/V1_0__initial.sql").exists())

        val file = File("${context.outputDirectory}/src/test/kotlin/com/wutsi/test/FlywayConfiguration.kt")
        assertTrue(file.exists())
        val text = file.readText()
        kotlin.test.assertEquals(
            """
                package com.wutsi.test

                import kotlin.Boolean
                import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
                import org.springframework.context.`annotation`.Bean
                import org.springframework.context.`annotation`.Configuration

                @Configuration
                public class FlywayConfiguration {
                  @Bean
                  public fun flywayMigrationStrategy(): FlywayMigrationStrategy = FlywayMigrationStrategy {
                      flyway ->
                      if (!cleaned) {
                          flyway.clean()
                          cleaned = true
                      }
                      flyway.migrate()
                  }

                  public companion object {
                    public var cleaned: Boolean = false
                  }
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }

    @Test
    fun `never overwrite initial SQL script`() {
        val openAPI = OpenAPI()
        context.addService(Context.SERVICE_DATABASE)

        val file = File("${context.outputDirectory}/src/main/resources/db/migration/V1_0__initial.sql")
        file.parentFile.mkdirs()
        Files.writeString(file.toPath(), "Foo")

        Thread.sleep(2000)
        codegen.generate(openAPI, context)

        assertTrue(System.currentTimeMillis() - file.lastModified() >= 2000)
    }
}
