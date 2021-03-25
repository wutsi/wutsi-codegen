package com.wutsi.codegen.kotlin

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.util.CaseUtil.toCamelCase
import com.wutsi.codegen.model.Api
import com.wutsi.codegen.model.Endpoint
import com.wutsi.codegen.model.EndpointParameter
import com.wutsi.codegen.model.Field
import com.wutsi.codegen.model.ParameterType.COOKIE
import com.wutsi.codegen.model.ParameterType.HEADER
import com.wutsi.codegen.model.ParameterType.PATH
import com.wutsi.codegen.model.ParameterType.QUERY
import com.wutsi.codegen.model.Request
import com.wutsi.codegen.model.Type
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.CookieParameter
import io.swagger.v3.oas.models.parameters.HeaderParameter
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.PathParameter
import io.swagger.v3.oas.models.parameters.QueryParameter
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponses
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.reflect.KClass

class KotlinMapper(private val context: Context) {
    companion object {
        val OPENAPI_TYPE_TO_KOLTIN: Map<String, KClass<*>> = mapOf(
            "string" to String::class,
            "string:date" to LocalDate::class,
            "string:date-time" to OffsetDateTime::class,
            "string:binary" to ByteArray::class,

            "number" to Double::class,
            "number:int32" to Int::class,
            "number:int64" to Long::class,
            "number:float" to Float::class,
            "number:double" to Double::class,

            "integer" to Int::class,
            "integer:int32" to Int::class,
            "integer:int64" to Long::class,

            "boolean" to Boolean::class,
            "array" to List::class,
            "object" to Any::class
        )
    }

    fun toAPI(openAPI: OpenAPI) = Api(
        packageName = context.basePackage,
        name = toCamelCase("${context.apiName}Api", true),
        endpoints = toEndpoints(openAPI)
    )

    fun toEndpoints(openAPI: OpenAPI): List<Endpoint> {
        val endpoints = mutableListOf<Endpoint>()
        openAPI.paths?.forEach { path, item ->
            item.get?.let { endpoints.add(toEndpoint(path, "GET", it)) }
            item.post?.let { endpoints.add(toEndpoint(path, "POST", it)) }
            item.delete?.let { endpoints.add(toEndpoint(path, "DELETE", it)) }
            item.put?.let { endpoints.add(toEndpoint(path, "PUT", it)) }
            item.trace?.let { endpoints.add(toEndpoint(path, "TRACE", it)) }
            item.patch?.let { endpoints.add(toEndpoint(path, "PATCH", it)) }
            item.head?.let { endpoints.add(toEndpoint(path, "HEAD", it)) }
        }
        return endpoints
    }

    fun toEndpoint(path: String, method: String, operation: Operation) = Endpoint(
        path = path,
        name = toCamelCase(operation.operationId?.let { it } ?: "${method.toLowerCase()}$path", false),
        method = method.toUpperCase(),
        request = operation.requestBody?.let { toRequest(it) } ?: null,
        response = toResponse(operation.responses),
        parameters = operation.parameters?.map { toParameter(it) } ?: emptyList()
    )

    fun toRequest(body: RequestBody): Request {
        val requests = mutableListOf<Request>()
        body.content.forEach { contentType, media ->
            requests.add(
                Request(
                    contentType = contentType,
                    type = context.getType(media.schema.`$ref`)!!,
                    required = body.required?.let { it } ?: false,
                )
            )
        }
        return requests[0]
    }

    fun toResponse(responses: ApiResponses?): Type? {
        if (responses == null || responses.isEmpty())
            return null

        val response = responses.values.toList()[0]
        if (response.content == null || response.content.isEmpty())
            return null

        val ref = response.content.values.toList()[0].schema.`$ref`
        return context.getType(ref)
    }

    fun toParameter(parameter: Parameter) = EndpointParameter(
        type = when (parameter.javaClass) {
            QueryParameter::class.java -> QUERY
            PathParameter::class.java -> PATH
            HeaderParameter::class.java -> HEADER
            CookieParameter::class.java -> COOKIE
            else -> throw IllegalStateException("Unsupported parameters ${parameter.javaClass}")
        },
        field = toField(parameter.name, parameter.schema),
        name = parameter.name
    )

    fun <T> toType(name: String, schema: Schema<T>) = Type(
        name = toCamelCase(name, true),
        packageName = toPackage(context.basePackage, "model"),
        fields = schema.properties?.map { toField(it.key, it.value, schema as Schema<Any>) } ?: emptyList()
    )

    fun <T> toField(name: String, property: Schema<T>, schema: Schema<T>? = null) = Field(
        name = toCamelCase(name, false),
        type = toKClass(property),
        parametrizedType = toParametrizedType(property),
        default = property.default?.toString(),
        required = schema?.required?.contains(name) == true,
        min = property.minimum,
        max = property.maximum,
        pattern = property.pattern,
        minLength = property.minLength,
        maxLength = property.maxLength,
        minItems = property.minItems,
        maxItems = property.maxItems,
        nullable = property.nullable?.let { it } ?: false
    )

    fun <T> toKClass(property: Schema<T>): KClass<*> {
        var result: KClass<*>? = null
        val type = property.type
        if (type == "array") {
            result = List::class
        } else {
            val format = property.format
            result = OPENAPI_TYPE_TO_KOLTIN["$type:$format"] ?: OPENAPI_TYPE_TO_KOLTIN[type]
        }

        if (result == null)
            throw IllegalStateException("Unable to resolve the type. ${property.name}:$type")

        return result
    }

    fun <T> toParametrizedType(property: Schema<T>): Type? {
        var result: KClass<*>? = null
        if (property.type == "array") {
            val ref = (property as ArraySchema).items?.`$ref`
            val type = ref?.let { context.getType(it) } ?: null
            if (type == null)
                throw IllegalStateException("Unable to resolve the reference: $ref")

            return type
        } else {
            return null
        }
    }

    private fun toPackage(basePackage: String, suffix: String): String =
        if (basePackage.isNullOrEmpty()) suffix else "$basePackage.$suffix"
}
