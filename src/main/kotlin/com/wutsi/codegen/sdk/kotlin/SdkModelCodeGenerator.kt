package com.wutsi.codegen.sdk.kotlin

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.wutsi.codegen.CodeGenerator
import com.wutsi.codegen.Context
import com.wutsi.codegen.model.Field
import com.wutsi.codegen.model.Type
import io.swagger.v3.oas.models.OpenAPI
import java.io.File
import java.time.LocalDate
import java.time.OffsetDateTime
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

class SdkModelCodeGenerator(private val mapper: KotlinMapper) : CodeGenerator {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val models = loadModels(openAPI, context)
        models.forEach { generateModel(it, context) }
    }

    private fun loadModels(spec: OpenAPI, context: Context): List<Type> {
        // Load
        spec.components.schemas.map { mapper.toType(it.key, it.value) }

        // Register
        val result = mutableListOf<Type>()
        spec.components.schemas.forEach { name, schema ->
            val type = mapper.toType(name, schema)
            result.add(type)

            mapper.register("#/components/schemas/$name", type)
        }
        return result
    }

    private fun generateModel(type: Type, context: Context) {
        val file = File(context.outputDirectory)
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
            .addAnnotations(toAnnotationSpecs(field))

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

    fun toAnnotationSpecs(field: Field): List<AnnotationSpec> {
        val annotations = mutableListOf<AnnotationSpec>()
        if (field.required) {
            if (field.type == String::class) {
                annotations.add(
                    AnnotationSpec.builder(NotBlank::class.java)
                        .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
                        .build()
                )
            } else {
                annotations.add(
                    AnnotationSpec.builder(NotNull::class.java)
                        .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
                        .build()
                )
                if (field.type == List::class) {
                    annotations.add(
                        AnnotationSpec.builder(NotEmpty::class.java)
                            .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
                            .build()
                    )
                }
            }
        }
        if (field.min != null) {
            annotations.add(
                AnnotationSpec.builder(Min::class.java)
                    .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
                    .addMember(field.min.toString())
                    .build()
            )
        }
        if (field.max != null) {
            annotations.add(
                AnnotationSpec.builder(Max::class.java)
                    .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
                    .addMember(field.max.toString())
                    .build()
            )
        }
        if (field.minLength != null || field.maxLength != null) {
            val builder = AnnotationSpec.builder(Size::class.java)
                .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)

            if (field.minLength != null)
                builder.addMember("min=" + field.minLength.toString())
            if (field.maxLength != null)
                builder.addMember("max=" + field.maxLength.toString())

            annotations.add(builder.build())
        }
        if (field.pattern != null) {
            annotations.add(
                AnnotationSpec.builder(Pattern::class.java)
                    .useSiteTarget(AnnotationSpec.UseSiteTarget.GET)
                    .addMember("\"${field.pattern}\"")
                    .build()
            )
        }
        return annotations
    }
}
