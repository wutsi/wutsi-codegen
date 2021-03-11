package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

internal class SdkCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = System.getProperty("user.home") + "/wutsi/codegen",
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

        // POM
        assertTrue(File("${context.outputDirectory}/pom.xml").exists())

        // API
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/TestApi.kt").exists())

        // Model files
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/ErrorResponse.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/CreateLikeRequest.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/CreateLikeResponse.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/GetStatsResponse.kt").exists())
    }
}
