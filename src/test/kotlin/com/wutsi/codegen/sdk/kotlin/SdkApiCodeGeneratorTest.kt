package com.wutsi.codegen.sdk.kotlin

import com.wutsi.codegen.Context
import com.wutsi.codegen.model.Api
import com.wutsi.codegen.model.Endpoint
import com.wutsi.codegen.model.EndpointParameter
import com.wutsi.codegen.model.Field
import com.wutsi.codegen.model.ParameterType.PATH
import com.wutsi.codegen.model.Request
import com.wutsi.codegen.model.Type
import org.junit.jupiter.api.Test

internal class SdkApiCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/codegen",
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
}
