package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.kotlin.sdk.SdkCodeGenerator
import com.wutsi.codegen.model.Endpoint
import com.wutsi.codegen.model.EndpointParameter
import com.wutsi.codegen.model.Field
import com.wutsi.codegen.model.ParameterType.QUERY
import com.wutsi.codegen.model.Request
import com.wutsi.codegen.model.Type
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.FileSystemUtils
import java.io.File
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ServerDelegateCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test"
    )

    val codegen = ServerDelegateCodeGenerator(KotlinMapper(context))

    @Test
    fun `toFuncSpec - requestBody`() {
        val endpoint = Endpoint(
            name = "create",
            method = "POST",
            path = "/v1/foo",
            response = Type(packageName = "com.wutsi.test.model", name = "CreateFooResponse"),
            request = Request(
                required = true,
                contentType = "application/json",
                Type(packageName = "com.wutsi.test.model", name = "CreateFooRquest")
            )
        )
        val result = codegen.toFunSpec(endpoint)
        assertEquals(
            """
                public fun invoke(request: com.wutsi.test.model.CreateFooRquest): com.wutsi.test.model.CreateFooResponse {
                  TODO()
                }
            """.trimIndent(),
            result.toString().trimIndent()
        )
    }

    @Test
    fun `toFuncSpec - parameter`() {
        val endpoint = Endpoint(
            name = "create",
            method = "POST",
            path = "/v1/foo",
            response = null,
            parameters = listOf(
                EndpointParameter(
                    name = "bar",
                    type = QUERY,
                    field = Field(name = "bar", type = String::class)
                )
            )
        )
        val result = codegen.toFunSpec(endpoint)
        assertEquals(
            """
                public fun invoke(bar: kotlin.String): kotlin.Unit {
                  TODO()
                }
            """.trimIndent(),
            result.toString().trimIndent()
        )
    }

    @Test
    fun `toTypeSpec`() {
        val endpoint = Endpoint(
            name = "create",
            method = "POST",
            path = "/v1/foo",
            response = null,
            parameters = listOf(
                EndpointParameter(
                    name = "bar",
                    type = QUERY,
                    field = Field(name = "bar", type = String::class)
                )
            )
        )

        val result = codegen.toTypeSpec(endpoint, context)
        assertEquals(
            """
                public class CreateDelegate {
                  public fun invoke(bar: kotlin.String): kotlin.Unit {
                    TODO()
                  }
                }
            """.trimIndent(),
            result.toString().trimIndent()
        )
    }

    @BeforeEach
    fun setUp() {
        FileSystemUtils.deleteRecursively(File(context.outputDirectory))
    }

    @Test
    fun `generate`() {
        val yaml = IOUtils.toString(SdkCodeGenerator::class.java.getResourceAsStream("/api.yaml"), "utf-8")

        context.register("#/components/schemas/ErrorResponse", Type(packageName = "${context.basePackage}.model", name = "ErrorResponse"))
        context.register("#/components/schemas/CreateLikeRequest", Type(packageName = "${context.basePackage}.model", name = "CreateLikeRequest"))
        context.register("#/components/schemas/CreateLikeResponse", Type(packageName = "${context.basePackage}.model", name = "CreateLikeResponse"))
        context.register("#/components/schemas/GetStatsResponse", Type(packageName = "${context.basePackage}.model", name = "GetStatsResponse"))

        codegen.generate(
            openAPI = OpenAPIV3Parser().readContents(yaml).openAPI,
            context = context
        )

        // Controller
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/CreateDelegate.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/DeleteDelegate.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/StatsDelegate.kt").exists())
    }

    @Test
    fun `generate - do not overwrite`() {
        val yaml = IOUtils.toString(SdkCodeGenerator::class.java.getResourceAsStream("/api.yaml"), "utf-8")

        context.register("#/components/schemas/ErrorResponse", Type(packageName = "${context.basePackage}.model", name = "ErrorResponse"))
        context.register("#/components/schemas/CreateLikeRequest", Type(packageName = "${context.basePackage}.model", name = "CreateLikeRequest"))
        context.register("#/components/schemas/CreateLikeResponse", Type(packageName = "${context.basePackage}.model", name = "CreateLikeResponse"))
        context.register("#/components/schemas/GetStatsResponse", Type(packageName = "${context.basePackage}.model", name = "GetStatsResponse"))

        val path = "${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/CreateDelegate.kt"
        File(path).parentFile.mkdirs()
        Files.write(
            File(path).toPath(),
            "xxx".toByteArray()
        )

        val delay = 5000L
        Thread.sleep(delay)
        codegen.generate(
            openAPI = OpenAPIV3Parser().readContents(yaml).openAPI,
            context = context
        )

        val file = File(path)
        assertTrue(System.currentTimeMillis() - file.lastModified() >= delay)
    }
}
