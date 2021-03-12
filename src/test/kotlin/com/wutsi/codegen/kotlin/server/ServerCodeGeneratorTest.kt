package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.kotlin.sdk.SdkCodeGenerator
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import java.io.File

internal class ServerCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test"
    )

    val codegen = SdkCodeGenerator(
        KotlinMapper(context)
    )

    @Test
    fun testGenerate() {
        val yaml = IOUtils.toString(SdkCodeGenerator::class.java.getResourceAsStream("/api.yaml"))
        codegen.generate(
            openAPI = OpenAPIV3Parser().readContents(yaml).openAPI,
            context = context
        )

        // Controller
        kotlin.test.assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/endpoint/CreateController.kt").exists())
        kotlin.test.assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/endpoint/DeleteController.kt").exists())
        kotlin.test.assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/endpoint/StatsController.kt").exists())

        // Controller
        kotlin.test.assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/CreateDelegate.kt").exists())
        kotlin.test.assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/DeleteDelegate.kt").exists())
        kotlin.test.assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/StatsDelegate.kt").exists())
    }
}
