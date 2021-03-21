package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.io.File

class ServerLauncherCodeGenerator : AbstractKotlinCodeGenerator() {
    companion object {
        const val CLASSNAME = "Application"
    }

    override fun generate(spec: OpenAPI, context: Context) {
        generateClass(context)
    }

    private fun generateClass(context: Context) {
        val directory = getSourceDirectory(context)
        val classname = ClassName(context.basePackage, CLASSNAME)
        val relativePath = classname.toString().replace('.', File.separatorChar)
        if (File(directory.absolutePath + File.separator + relativePath + ".kt").exists())
            return

        System.out.println("Generating $classname to $directory")
        FileSpec.builder(classname.packageName, classname.simpleName)
            .addType(toTypeSpec(context))
            .addFunction(toFunSpec())
            .build()
            .writeTo(getSourceDirectory(context))
    }

    private fun toTypeSpec(context: Context): TypeSpec {
        val spec = TypeSpec.classBuilder(ClassName(context.basePackage, CLASSNAME))
            .addAnnotation(SpringBootApplication::class)

        if (context.hasService(Context.SERVICE_DATABASE))
            spec.addAnnotation(EnableTransactionManagement::class.java)
        if (context.hasService(Context.SERVICE_CACHE))
            spec.addAnnotation(EnableCaching::class.java)

        return spec.build()
    }

    private fun toFunSpec(): FunSpec =
        FunSpec.builder("main")
            .addParameter("args", String::class, KModifier.VARARG)
            .addCode(
                CodeBlock.of("org.springframework.boot.runApplication<Application>(*args)")
            )
            .build()
}
