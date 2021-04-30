package com.wutsi.codegen.helpers

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.CodeGenerator
import com.wutsi.codegen.kotlin.sdk.SdkCodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.BeforeEach
import org.springframework.util.FileSystemUtils
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal abstract class AbstractMustacheCodeGeneratorTest {
    companion object {
        const val DELAY = 2000L
    }

    abstract fun createContext(): Context

    abstract fun getCodeGenerator(context: Context): CodeGenerator

    @BeforeEach
    fun setUp() {
        FileSystemUtils.deleteRecursively(File(createContext().outputDirectory))
    }

    fun assertContent(expectedResourcePath: String, filePath: String) {
        val file = File(filePath)
        assertTrue(file.exists())

        val expected = IOUtils.toString(AbstractMustacheCodeGeneratorTest::class.java.getResourceAsStream(expectedResourcePath), "utf-8")
        assertEquals(expected.trimIndent().trim(), file.readText().trimIndent().trim())
    }

    fun assertFileNotOverwritten(filePath: String) {
        val file = File(filePath)
        assertTrue(file.exists())

        assertTrue(System.currentTimeMillis() - file.lastModified() >= DELAY)
    }

    fun assertFileOverwritten(filePath: String) {
        val file = File(filePath)
        assertTrue(file.exists())

        assertTrue(System.currentTimeMillis() - file.lastModified() < DELAY)
    }

    fun createFileAndWait(
        filePath: String,
        content: String = "xxx",
    ) {
        File(filePath).parentFile.mkdirs()
        Files.write(
            File(filePath).toPath(),
            content.toByteArray()
        )

        Thread.sleep(DELAY)
    }

    protected fun createOpenAPI(withSecurity: Boolean = false): OpenAPI {
        if (withSecurity) {
            val yaml = IOUtils.toString(SdkCodeGenerator::class.java.getResourceAsStream("/api.yaml"), "utf-8")
            return OpenAPIV3Parser().readContents(yaml).openAPI
        } else {
            val openAPI = OpenAPI()
            openAPI.info = Info()
            openAPI.info.title = "test"
            openAPI.info.version = "1.3.7"
            openAPI.info.description = "This api used for managing likes on links"
            return openAPI
        }
    }
}
