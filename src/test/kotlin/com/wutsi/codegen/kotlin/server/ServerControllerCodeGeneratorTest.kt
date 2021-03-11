package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.kotlin.sdk.SdkCodeGenerator
import com.wutsi.codegen.model.Endpoint
import com.wutsi.codegen.model.EndpointParameter
import com.wutsi.codegen.model.Field
import com.wutsi.codegen.model.ParameterType
import com.wutsi.codegen.model.ParameterType.HEADER
import com.wutsi.codegen.model.ParameterType.PATH
import com.wutsi.codegen.model.ParameterType.QUERY
import com.wutsi.codegen.model.Request
import com.wutsi.codegen.model.Type
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class ServerControllerCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test"
    )

    val codegen = ServerControllerCodeGenerator(KotlinMapper(context))

    @Test
    fun `toRequestMappingClass`() {
        val field = Field(name = "bar", String::class)
        assertEquals(GetMapping::class, codegen.toRequestMappingClass(Endpoint(name = "xx", path = "xx", method = "GET")))
        assertEquals(PostMapping::class, codegen.toRequestMappingClass(Endpoint(name = "xx", path = "xx", method = "post")))
        assertEquals(DeleteMapping::class, codegen.toRequestMappingClass(Endpoint(name = "xx", path = "xx", method = "DELETE")))
        assertEquals(PutMapping::class, codegen.toRequestMappingClass(Endpoint(name = "xx", path = "xx", method = "put")))

        assertThrows<IllegalStateException> {
            codegen.toRequestMappingClass(Endpoint(name = "xx", path = "xx", method = "trace"))
        }
    }

    @Test
    fun `toParameterType`() {
        val field = Field(name = "bar", String::class)
        assertEquals(PathVariable::class, codegen.toParameterType(EndpointParameter(type = ParameterType.PATH, name = "foo", field = field)))
        assertEquals(RequestHeader::class, codegen.toParameterType(EndpointParameter(type = ParameterType.HEADER, name = "foo", field = field)))
        assertEquals(RequestParam::class, codegen.toParameterType(EndpointParameter(type = ParameterType.QUERY, name = "foo", field = field)))

        assertThrows<IllegalStateException> {
            codegen.toParameterType(EndpointParameter(type = ParameterType.COOKIE, name = "foo", field = field))
        }
    }

    @Test
    fun `toParameterSpec - PathRequest`() {
        val param = EndpointParameter(
            type = PATH,
            name = "id",
            field = Field("id", String::class, required = true, default = "hello")
        )
        val result = codegen.toParameter(param)
        assertEquals(
            "@org.springframework.web.bind.`annotation`.PathVariable(name=\"id\", default=\"hello\") @get:javax.validation.constraints.NotBlank id: kotlin.String",
            result.toString()
        )
    }

    @Test
    fun `toParameterSpec - RequestHeader`() {
        val param = EndpointParameter(
            type = HEADER,
            name = "id",
            field = Field("id", String::class)
        )
        val result = codegen.toParameter(param)
        assertEquals(
            "@org.springframework.web.bind.`annotation`.RequestHeader(name=\"id\", required=\"false\") id: kotlin.String",
            result.toString()
        )
    }

    @Test
    fun `toParameterSpec - QueryParam`() {
        val param = EndpointParameter(
            type = QUERY,
            name = "id",
            field = Field("id", String::class)
        )
        val result = codegen.toParameter(param)
        assertEquals(
            "@org.springframework.web.bind.`annotation`.RequestParam(name=\"id\", required=\"false\") id: kotlin.String",
            result.toString()
        )
    }

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
                @org.springframework.web.bind.`annotation`.PostMapping("/v1/foo")
                public fun invoke(@javax.validation.Valid @org.springframework.web.bind.`annotation`.RequestBody request: com.wutsi.test.model.CreateFooRquest): com.wutsi.test.model.CreateFooResponse {
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
                @org.springframework.web.bind.`annotation`.PostMapping("/v1/foo")
                public fun invoke(@org.springframework.web.bind.`annotation`.RequestParam(name="bar", required="false") bar: kotlin.String): kotlin.Unit {
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

        val result = codegen.toTypeSpec(endpoint)
        assertEquals(
            """
                @org.springframework.web.bind.`annotation`.RestController
                public class CreateController {
                  @org.springframework.web.bind.`annotation`.PostMapping("/v1/foo")
                  public fun invoke(@org.springframework.web.bind.`annotation`.RequestParam(name="bar", required="false") bar: kotlin.String): kotlin.Unit {
                  }
                }
            """.trimIndent(),
            result.toString().trimIndent()
        )
    }

    @Test
    fun testGenerate() {
        val yaml = IOUtils.toString(SdkCodeGenerator::class.java.getResourceAsStream("/api.yaml"))

        context.register("#/components/schemas/ErrorResponse", Type(packageName = "${context.basePackage}.model", name = "ErrorResponse"))
        context.register("#/components/schemas/CreateLikeRequest", Type(packageName = "${context.basePackage}.model", name = "CreateLikeRequest"))
        context.register("#/components/schemas/CreateLikeResponse", Type(packageName = "${context.basePackage}.model", name = "CreateLikeResponse"))
        context.register("#/components/schemas/GetStatsResponse", Type(packageName = "${context.basePackage}.model", name = "GetStatsResponse"))

        codegen.generate(
            openAPI = OpenAPIV3Parser().readContents(yaml).openAPI,
            context = context
        )

        // Controller
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/CreateController.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/DeleteController.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/StatsController.kt").exists())
    }
}
