package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.kotlin.sdk.SdkCodeGenerator
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

internal class ServerCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test"
    )

    val codegen = ServerCodeGenerator(
        KotlinMapper(context)
    )

    @Test
    fun testGenerate() {
        val yaml = IOUtils.toString(SdkCodeGenerator::class.java.getResourceAsStream("/api.yaml"), "utf-8")
        codegen.generate(
            openAPI = OpenAPIV3Parser().readContents(yaml).openAPI,
            context = context
        )

        // Controller
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/endpoint/CreateController.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/endpoint/DeleteController.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/endpoint/StatsController.kt").exists())

        // Delegate
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/CreateDelegate.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/DeleteDelegate.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/StatsDelegate.kt").exists())

        // Model files
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/ErrorResponse.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/CreateLikeRequest.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/CreateLikeResponse.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/GetStatsResponse.kt").exists())

        // Pom
        assertTrue(File("${context.outputDirectory}/pom.xml").exists())
    }
}
