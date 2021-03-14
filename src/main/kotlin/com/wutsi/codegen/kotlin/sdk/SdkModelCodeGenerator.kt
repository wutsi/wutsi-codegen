package com.wutsi.codegen.kotlin.sdk

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Field
import com.wutsi.codegen.model.Type
import io.swagger.v3.oas.models.OpenAPI
import java.time.LocalDate
import java.time.OffsetDateTime

class SdkModelCodeGenerator(private val mapper: KotlinMapper) : AbstractKotlinCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val models = loadModels(openAPI, context)
        models.forEach { generateModel(it, context) }
    }

    fun loadModels(spec: OpenAPI, context: Context): List<Type> {
        // Load
        spec.components.schemas.map { mapper.toType(it.key, it.value) }

        // Register
        val result = mutableListOf<Type>()
        spec.components.schemas.forEach { name, schema ->
            val type = mapper.toType(name, schema)
            result.add(type)

            context.register("#/components/schemas/$name", type)
        }
        return result
    }

    private fun generateModel(type: Type, context: Context) {
        val file = getSourceDirectory(context)
        System.out.println("Generating ${type.packageName}.${type.name} to $file")
        FileSpec.builder(type.packageName, type.name)
            .addType(toModelTypeSpec(type))
            .build()
            .writeTo(file)
    }

    fun toModelTypeSpec(type: Type): TypeSpec {
        val spec = TypeSpec.classBuilder(type.name)
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(type.fields.map { toParameterSpec(it) })
                    .build()
            )
            .addProperties(type.fields.map { toPropertySpec(it) })
            .build()
        return spec
    }

    private fun toPropertySpec(field: Field): PropertySpec =
        PropertySpec.builder(field.name, field.type.asTypeName().copy(field.nullable))
            .initializer(field.name)
            .mutable(false)
            .build()

    fun toParameterSpec(field: Field): ParameterSpec {
        val builder = ParameterSpec.builder(field.name, field.type.asTypeName().copy(field.nullable))
            .addAnnotations(toValidationAnnotationSpecs(field))

        val default = defaultValue(field)
        if (default != null)
            builder.defaultValue(default)

        return builder.build()
    }

    fun defaultValue(field: Field): String? {
        if (field.default == null && field.nullable) {
            return "null"
        } else if (field.default == null && !field.nullable) {
            return when (field.type) {
                String::class -> "\"\""
                Int::class -> "0"
                Long::class -> "0"
                Float::class -> "0"
                Double::class -> "0"
                LocalDate::class -> "LocalDate.now()"
                OffsetDateTime::class -> "OffsetDateTime.now()"
                List::class -> "emptyList()"
                else -> return null
            }
        } else {
            return when (field.type) {
                String::class -> if (field.default.isNullOrEmpty()) "\"\"" else "\"${field.default}\""
                Int::class -> field.default
                Long::class -> field.default
                Float::class -> field.default
                Double::class -> field.default
                else -> return null
            }
        }
    }
}
