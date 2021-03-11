package com.wutsi.codegen.kotlin

import com.wutsi.codegen.Context
import com.wutsi.codegen.model.ParameterType
import com.wutsi.codegen.model.Type
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.CookieParameter
import io.swagger.v3.oas.models.parameters.HeaderParameter
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.PathParameter
import io.swagger.v3.oas.models.parameters.QueryParameter
import io.swagger.v3.oas.models.parameters.RequestBody
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class KotlinMapperTest {
    private val context = Context(
        apiName = "Test",
        basePackage = "com.wutsi.codegen.test",
        outputDirectory = "./target/codegen",
        artifactId = "wutsi-test",
        groupId = "x.y.z",
        jdkVersion = "1.8"
    )
    private val mapper = KotlinMapper(context)

    @Test
    fun `toType`() {
        val schema = createEntity()

        val type = mapper.toType("Foo", schema)

        assertEquals("Foo", type.name)
        assertEquals("com.wutsi.codegen.test.model", type.packageName)
        assertEquals(2, type.fields.size)
    }

    @ParameterizedTest
    @MethodSource("camelCaseTypeDataProvider")
    fun `toType - name in camel case`(name: String, expected: String) {
        val schema = Schema<String>()

        val type = mapper.toType(name, schema)

        assertEquals(expected, type.name)
    }

    @Test
    fun `toField`() {
        val schema = Schema<String>()

        val property = Schema<String>()
        property.type = "string"
        property.nullable = false
        property.minimum = BigDecimal(5)
        property.maximum = BigDecimal(10)
        property.minItems = 6
        property.maxItems = 11
        property.minItems = 7
        property.maxLength = 12
        property.setDefault("xxx")

        val field = mapper.toField("foo", property, schema)
        assertEquals("foo", field.name)
        assertEquals(String::class, field.type)
        assertEquals(property.nullable, field.nullable)
        assertEquals(property.minimum, field.min)
        assertEquals(property.maximum, field.max)
        assertEquals(property.minLength, field.minLength)
        assertEquals(property.maxLength, field.maxLength)
        assertEquals(property.minItems, field.minItems)
        assertEquals(property.maxItems, field.maxItems)
    }

    @Test
    fun `toField - non-required property`() {
        val schema = Schema<String>()

        val property = Schema<String>()
        property.type = "string"

        val field = mapper.toField("foo", property, schema)
        assertFalse(field.required)
    }

    @Test
    fun `toField - required property`() {
        val schema = Schema<String>()
        schema.required = listOf("yo", "man", "foo")

        val property = Schema<String>()
        property.type = "string"

        val field = mapper.toField("foo", property, schema)
        assertTrue(field.required)
    }

    @ParameterizedTest
    @MethodSource("camelCaseFieldDataProvider")
    fun `toField - name in Camel case`(name: String, expected: String) {
        val schema = Schema<String>()
        val property = Schema<String>()
        property.type = "string"

        val field = mapper.toField(name, property, schema)
        assertEquals(expected, field.name)
    }

    @ParameterizedTest
    @MethodSource("fieldTypeDataProvider")
    fun `map OpenAPI type to Kotlin class`(type: String, format: String?, expected: KClass<*>) {
        val property = Schema<String>()
        property.name = "test"
        property.type = type
        property.format = format

        assertEquals(expected, mapper.toKClass(property))
    }

    @Test
    fun `throw Exception on invalid OpenAPI type`() {
        val property = Schema<String>()
        property.name = "test"
        property.type = "xxxx"

        assertThrows<IllegalStateException> { mapper.toKClass(property) }
    }

    @Test
    fun `map Query parameter`() {
        val param = QueryParameter()
        param.name = "foo"
        param.schema = createProperty()

        val result = mapper.toParameter(param)
        assertEquals(ParameterType.QUERY, result.type)
        assertEquals(param.name, result.field.name)
        assertEquals(String::class, result.field.type)
    }

    @Test
    fun `map Path parameter`() {
        val param = PathParameter()
        param.name = "foo"
        param.schema = createProperty()

        val result = mapper.toParameter(param)
        assertEquals(ParameterType.PATH, result.type)
        assertEquals(param.name, result.field.name)
        assertEquals(String::class, result.field.type)
    }

    @Test
    fun `map Header parameter`() {
        val param = HeaderParameter()
        param.name = "foo"
        param.schema = createProperty()

        val result = mapper.toParameter(param)
        assertEquals(ParameterType.HEADER, result.type)
        assertEquals(param.name, result.field.name)
        assertEquals(String::class, result.field.type)
    }

    @Test
    fun `map Cookie parameter`() {
        val param = CookieParameter()
        param.name = "foo"
        param.schema = createProperty()

        val result = mapper.toParameter(param)
        assertEquals(ParameterType.COOKIE, result.type)
        assertEquals(param.name, result.field.name)
        assertEquals(String::class, result.field.type)
    }

    @Test
    fun `toRequest`() {
        val mediaType = MediaType()
        mediaType.schema = Schema<String>()
        mediaType.schema.`$ref` = "#/components/schemas/TestResponse"

        val type = Type(packageName = "x.y.z", name = "XXX")
        context.register(mediaType.schema.`$ref`, type)

        val requestBody = RequestBody()
        requestBody.content = Content()
        requestBody.content.addMediaType("application/json", mediaType)
        requestBody.required = true

        val result = mapper.toRequest(requestBody)

        assertEquals("application/json", result.contentType)
        assertEquals(requestBody.required, result.required)
        assertEquals(type, result.type)
    }

    @Test
    fun `map endpoint with request body`() {
        val mediaType = MediaType()
        mediaType.schema = Schema<String>()
        mediaType.schema.`$ref` = "#/components/schemas/TestResponse"

        val type = Type(packageName = "x.y.z", name = "XXX")
        context.register(mediaType.schema.`$ref`, type)

        val operation = Operation()
        operation.operationId = "get-by-id"
        operation.requestBody = RequestBody()
        operation.requestBody.content = Content()
        operation.requestBody.content.addMediaType("application/json", mediaType)

        val result = mapper.toEndpoint("/v1/test", "get", operation)

        assertEquals("/v1/test", result.path)
        assertEquals("getById", result.name)
        assertEquals("GET", result.method)
        assertEquals("application/json", result.request?.contentType)
        assertEquals(type, result.request?.type)
        assertTrue(result.parameters.isEmpty())
    }

    @Test
    fun `map endpoint with parameter`() {
        val param = HeaderParameter()
        param.name = "foo"
        param.schema = createProperty()

        val operation = Operation()
        operation.operationId = "get-by-id"
        operation.parameters = mutableListOf<Parameter>()
        operation.parameters.add(param)

        val result = mapper.toEndpoint("/v1/test", "get", operation)

        assertEquals("/v1/test", result.path)
        assertEquals("getById", result.name)
        assertEquals("GET", result.method)
        assertNull(result.request)
        assertEquals(1, result.parameters.size)
        assertEquals("foo", result.parameters[0].field.name)
    }

    @Test
    fun `map endpoint with no operation-id`() {
        val operation = Operation()
        val result = mapper.toEndpoint("/v1/test", "GET", operation)

        assertEquals("getV1Test", result.name)
    }

    @ParameterizedTest
    @ValueSource(strings = ["get", "post", "delete", "put", "patch", "trace", "head"])
    fun `map GET endpoint`(method: String) {
        val openAPI = OpenAPI()
        openAPI.paths = createPaths(method)

        val result = mapper.toEndpoints(openAPI)

        val endpoint = result[0]
        assertEquals(method, endpoint.method.toLowerCase())
    }

    @Test
    fun `toPom`() {
        val openAPI = createOpenAPI()

        val result = mapper.toPom(openAPI)

        assertEquals(4, result.size)
        assertEquals(openAPI.info.version, result["version"])
        assertEquals(context.artifactId, result["artifactId"])
        assertEquals(context.groupId, result["groupId"])
        assertEquals(context.jdkVersion, result["jdkVersion"])
    }

    @Test
    fun `toPom - no version`() {
        val result = mapper.toPom(OpenAPI())
        assertEquals("1.0.0", result["version"])
    }

    @Test
    fun `toPom - no groupId`() {
        val openAPI = createOpenAPI()

        val context = Context(
            apiName = "Test",
            basePackage = "com.wutsi.codegen.test",
            outputDirectory = "./target/codegen",
            artifactId = "wutsi-test",
            groupId = null,
            jdkVersion = "1.8"
        )
        val mapper = KotlinMapper(context)

        val result = mapper.toPom(openAPI)
        assertEquals(context.basePackage, result["groupId"])
    }

    @Test
    fun `toPom - no artifactId`() {
        val openAPI = createOpenAPI()

        val context = Context(
            apiName = "Wutsi Test",
            basePackage = "com.wutsi.codegen.test",
            outputDirectory = "./target/codegen",
            artifactId = null,
            groupId = "x.y.z",
            jdkVersion = "1.8"
        )
        val mapper = KotlinMapper(context)

        val result = mapper.toPom(openAPI)
        assertEquals("wutsi-test", result["artifactId"])
    }

    @Test
    fun `toApi`() {
        val result = mapper.toAPI(createOpenAPI())

        assertEquals("TestApi", result.name)
        assertEquals(context.basePackage, result.packageName)
    }

    private fun createOpenAPI(): OpenAPI {
        val openAPI = OpenAPI()
        openAPI.info = Info()
        openAPI.info.version = "1.3.7"
        return openAPI
    }

    private fun createPaths(method: String): Paths {
        val operation = Operation()
        operation.operationId = "get-by-id"

        val item = PathItem()
        if (method == "get") {
            item.get = operation
        } else if (method == "post") {
            item.post = operation
        } else if (method == "delete") {
            item.delete = operation
        } else if (method == "put") {
            item.put = operation
        } else if (method == "patch") {
            item.patch = operation
        } else if (method == "trace") {
            item.trace = operation
        } else if (method == "head") {
            item.head = operation
        }

        val paths = Paths()
        paths.addPathItem("/foo/bar", item)
        return paths
    }

    private fun createEntity(name: String = "Test"): Schema<String> {
        val schema = Schema<String>()
        schema.name = name
        schema.properties = mapOf(
            "property1" to createProperty(),
            "property2" to createProperty()
        )
        return schema
    }

    private fun createProperty(): Schema<String> {
        val property = Schema<String>()
        property.type = "string"
        return property
    }

    companion object {
        @JvmStatic
        fun fieldTypeDataProvider() = listOf(
            Arguments.of("string", null, String::class),
            Arguments.of("string", "date", LocalDate::class),
            Arguments.of("string", "date-time", OffsetDateTime::class),
            Arguments.of("string", "uuid", String::class),
            Arguments.of("string", "uri", String::class),
            Arguments.of("string", "binary", ByteArray::class),
            Arguments.of("string", "xxx", String::class),

            Arguments.of("number", null, Double::class),
            Arguments.of("number", "float", Float::class),
            Arguments.of("number", "double", Double::class),
            Arguments.of("number", "int32", Int::class),
            Arguments.of("number", "int64", Long::class),
            Arguments.of("number", "xxx", Double::class),

            Arguments.of("integer", null, Int::class),
            Arguments.of("integer", "int32", Int::class),
            Arguments.of("integer", "int64", Long::class),
            Arguments.of("integer", "xxx", Int::class),

            Arguments.of("boolean", null, Boolean::class),
            Arguments.of("boolean", "xxx", Boolean::class),

            Arguments.of("array", null, List::class),
            Arguments.of("array", "xxx", List::class),

            Arguments.of("object", null, Any::class),
            Arguments.of("object", "xxx", Any::class)
        )

        @JvmStatic
        fun camelCaseTypeDataProvider() = listOf(
            Arguments.of("request-context", "RequestContext"),
            Arguments.of("requestContext", "RequestContext"),
            Arguments.of("request context", "RequestContext"),
            Arguments.of("request_context", "RequestContext")
        )

        @JvmStatic
        fun camelCaseFieldDataProvider() = listOf(
            Arguments.of("request-context", "requestContext"),
            Arguments.of("requestContext", "requestContext"),
            Arguments.of("request context", "requestContext"),
            Arguments.of("request_context", "requestContext")
        )
    }
}
