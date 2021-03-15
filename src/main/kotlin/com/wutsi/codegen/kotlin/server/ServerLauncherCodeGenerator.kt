package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.wutsi.codegen.CodeGenerator
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

class ServerMainCodeGenerator(mapper: KotlinMapper) : CodeGenerator {
    companion object {
        const val CLASSNAME = "Application"
    }

    override fun generate(spec: OpenAPI, context: Context) {
        TODO("Not yet implemented")
    }

    fun generateClass(context: Context) {
        val file = File(context.basePackage, "Application.kt")
        if (file.exists())
            return

        System.out.println("Generating $file")
        FileSpec.builder(context.basePackage, CLASSNAME)
            .addType(toTypeSpec(context))
            .build()
            .writeTo(file)
    }

    fun toTypeSpec(context: Context): TypeSpec =
        TypeSpec.classBuilder(ClassName(context.basePackage, "Application"))
            .addFunction(toFunSpec())
            .build()

    fun toFunSpec(): FunSpec =
        FunSpec.builder("main")
            .addParameter("args", Array<String>::class)
            .addCode(
                CodeBlock.of("org.springframework.boot.runApplication<Application>(*args)")
            )
            .build()
}
