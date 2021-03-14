package com.wutsi.codegen.kotlin

import com.squareup.kotlinpoet.AnnotationSpec
import com.wutsi.codegen.CodeGenerator
import com.wutsi.codegen.Context
import com.wutsi.codegen.model.Field
import java.io.File
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
}
