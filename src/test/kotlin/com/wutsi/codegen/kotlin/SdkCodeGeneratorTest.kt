package com.wutsi.codegen.kotlin

import com.wutsi.codegen.Context
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

internal class SdkCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target",
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

        // Model files
        assertTrue(File("target/generated-sources/kotlin/com/wutsi/test/model/ErrorResponse.kt").exists())
        assertTrue(File("target/generated-sources/kotlin/com/wutsi/test/model/CreateLikeRequest.kt").exists())
        assertTrue(File("target/generated-sources/kotlin/com/wutsi/test/model/CreateLikeResponse.kt").exists())
        assertTrue(File("target/generated-sources/kotlin/com/wutsi/test/model/GetStatsResponse.kt").exists())

        // API
        assertTrue(File("target/generated-sources/kotlin/com/wutsi/test/TestApi.kt").exists())
    }
}
