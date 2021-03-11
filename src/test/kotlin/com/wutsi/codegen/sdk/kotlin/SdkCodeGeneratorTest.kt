package com.wutsi.codegen.sdk.kotlin

import com.wutsi.codegen.Context
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertEquals

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

        // List all the files
        var kt = 0
        var pom = 0
        Files.walk(Paths.get(context.outputDirectory))
            .filter(Files::isRegularFile)
            .forEach {
                var filepath = it.toFile().absolutePath
                if (filepath.endsWith(".kt")) {
                    System.out.println(">> Class generated: $filepath")
                    kt++
                } else if (filepath.endsWith("pom.xml")) {
                    System.out.println(">> pom generated: $filepath")
                    pom++
                }
            }

        assertEquals(1, pom)
        assertEquals(5, kt) // 4 model files, 1 API file
    }
}
