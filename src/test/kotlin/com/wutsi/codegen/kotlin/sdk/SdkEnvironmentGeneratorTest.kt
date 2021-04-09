package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SdkEnvironmentGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/sdk",
        basePackage = "com.wutsi.test"
    )

    val codegen = SdkEnvironmentGenerator()

    @Test
    fun `generate`() {
        val yaml = IOUtils.toString(SdkCodeGenerator::class.java.getResourceAsStream("/api.yaml"), "utf-8")
        codegen.generate(
            openAPI = OpenAPIV3Parser().readContents(yaml).openAPI,
            context = context
        )

        val file = File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/Environment.kt")
        assertTrue(file.exists())

        val text = file.readText()
        assertEquals(
            """
                package com.wutsi.test

                import kotlin.String

                public enum class Environment(
                  public val url: String
                ) {
                  PRODUCTION("https://app-prod.herokuapp.com"),
                  SANDBOX("https://app-sandbox.herokuapp.com"),
                  ;
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }
}
