package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SdkPomCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = System.getProperty("user.home") + "/wutsi/codegen",
        basePackage = "com.wutsi.test",
        artifactId = "wutsi-test",
        groupId = "x.y.z",
        jdkVersion = "1.8"
    )

    @Test
    fun generate() {
        val openAPI = OpenAPI()
        openAPI.info = Info()
        openAPI.info.version = "1.3.7"

        val codegen = SdkPomCodeGenerator(KotlinMapper(context))
        codegen.generate(OpenAPI(), context)

        val file = File("${context.outputDirectory}/pom.xml")
        assertTrue(file.exists())

        val result = file.readText()
        val expected = IOUtils.toString(SdkPomCodeGenerator::class.java.getResourceAsStream("/sdk/kotlin/pom.xml"))
        assertEquals(expected.trimIndent(), result.trimIndent())
    }
}
