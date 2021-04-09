package com.wutsi.codegen.kotlin.sdk

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.ENUM
import com.squareup.kotlinpoet.KModifier.PUBLIC
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.core.util.CaseUtil
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import io.swagger.v3.oas.models.OpenAPI

class SdkEnvironmentGenerator : AbstractKotlinCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        if (openAPI.servers.isEmpty())
            return

        val file = getSourceDirectory(context)

        System.out.println("Generating Environment enum to $file")
        FileSpec.builder(context.basePackage, "Environment")
            .addType(toModelTypeSpec(openAPI, context))
            .build()
            .writeTo(file)
    }

    private fun toModelTypeSpec(openAPI: OpenAPI, context: Context): TypeSpec {
        val spec = TypeSpec.classBuilder(toClassname(context))
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("url", String::class, PUBLIC)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("url", String::class)
                    .initializer("url")
                    .build()
            )
            .addModifiers(ENUM, PUBLIC)

        openAPI.servers.forEach {
            spec.addEnumConstant(
                CaseUtil.toCamelCase(it.description, false).toUpperCase(),
                TypeSpec.anonymousClassBuilder()
                    .addSuperclassConstructorParameter("%S", it.url)
                    .build()
            )
        }
        return spec.build()
    }

    fun toClassname(context: Context): ClassName = ClassName(context.basePackage, "Environment")
}
