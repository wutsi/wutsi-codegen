package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Api
import com.wutsi.codegen.model.Endpoint
import com.wutsi.codegen.model.EndpointParameter
import com.wutsi.codegen.model.Field
import com.wutsi.codegen.model.ParameterType.PATH
import com.wutsi.codegen.model.Request
import com.wutsi.codegen.model.Type
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

internal class SdkApiCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = System.getProperty("user.home") + "/wutsi/codegen/sdk",
        basePackage = "com.wutsi.test"
    )

    val codegen = SdkApiCodeGenerator(
        KotlinMapper(context)
    )

    @Test
    fun testToAPITypeSpec() {
        val api = Api(
            packageName = "com.wutsi.test",
            name = "Test",
            endpoints = listOf(
                Endpoint(
                    name = "getById",
                    path = "/foo/{id}",
                    method = "GET",
                    parameters = listOf(
                        EndpointParameter(
                            type = PATH,
                            name = "id",
                            field = Field(
                                name = "id",
                                type = Long::class
                            )
                        )
                    ),
                    response = Type(
                        packageName = "com.wutsi.test.model",
                        name = "GetFooResponse"
                    )
                ),
                Endpoint(
                    name = "deleteById",
                    path = "/foo/{id}",
                    method = "DELETE",
                    parameters = listOf(
                        EndpointParameter(
                            type = PATH,
                            name = "id",
                            field = Field(
                                name = "id",
                                type = Long::class
                            )
                        )
                    )
                ),
                Endpoint(
                    name = "create",
                    path = "/foo",
                    method = "POST",
                    request = Request(
                        required = true,
                        contentType = "application/json",
                        type = Type(
                            packageName = "com.wutsi.test.model",
                            name = "CreateFooRequest"
                        )
                    ),
                    response = Type(
                        packageName = "com.wutsi.test.model",
                        name = "CreateFooResponse"
                    )
                )
            )
        )

        val spec = codegen.toAPITypeSpec(api)

        val expected = """
            public interface Test {
              @feign.RequestLine("GET /foo/{id}")
              public fun getById(@feign.Param("id") id: kotlin.Long): com.wutsi.test.model.GetFooResponse

              @feign.RequestLine("DELETE /foo/{id}")
              public fun deleteById(@feign.Param("id") id: kotlin.Long): kotlin.Unit

              @feign.RequestLine("POST /foo")
              public fun create(): com.wutsi.test.model.CreateFooResponse
            }
        """.trimIndent()
        kotlin.test.assertEquals(expected, spec.toString().trimIndent())
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

        // API
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/TestApi.kt").exists())
    }
}