package com.wutsi.codegen.kotlin

import com.squareup.kotlinpoet.AnnotationSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.CodeGenerator
import com.wutsi.codegen.model.Field
import java.io.File
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.Date
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

abstract class AbstractKotlinCodeGenerator : CodeGenerator {
    protected fun getSourceDirectory(context: Context): File =
        File(context.outputDirectory + "${File.separator}src${File.separator}main${File.separator}kotlin")

    fun toValidationAnnotationSpecs(field: Field): List<AnnotationSpec> {
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

    fun defaultValue(field: Field, nonNullableDefault: Boolean = false): String? {
        if (field.default == null && field.nullable) {
            return "null"
        } else if (field.default == null && !field.nullable) {
            if (!nonNullableDefault)
                return null

            return when (field.type) {
                String::class -> "\"\""
                Boolean::class -> "false"
                Int::class -> "0"
                Long::class -> "0"
                Float::class -> "0"
                Double::class -> "0"
                Date::class -> "Date()"
                LocalDate::class -> "LocalDate.now()"
                OffsetDateTime::class -> "OffsetDateTime.now()"
                List::class -> "emptyList()"
                Any::class -> if (field.parametrizedType != null) "${field.parametrizedType.name}()" else null
                else -> return null
            }
        } else {
            return when (field.type) {
                String::class -> if (field.default.isNullOrEmpty()) "\"\"" else "\"${field.default}\""
                Int::class -> field.default
                Long::class -> field.default
                Float::class -> field.default
                Double::class -> field.default
                Boolean::class -> field.default
                else -> return null
            }
        }
    }
}
