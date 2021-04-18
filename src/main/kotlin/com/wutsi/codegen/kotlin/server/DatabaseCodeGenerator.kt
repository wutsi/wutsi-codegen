package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.File
import java.nio.file.Files

class DatabaseCodeGenerator : AbstractKotlinCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        if (!context.hasService(Context.SERVICE_DATABASE))
            return

        generateFlywayConfiguration(context)
        generateFlywayMigrationFile(context)
    }

    private fun generateFlywayConfiguration(context: Context) {
        val directory = getTestDirectory(context)
        val classname = ClassName(context.basePackage, "FlywayConfiguration")

        System.out.println("Generating $classname to $directory")
        FileSpec.builder(classname.packageName, classname.simpleName)
            .addType(
                TypeSpec.classBuilder(classname)
                    .addAnnotation(Configuration::class)
                    .addType(
                        TypeSpec.companionObjectBuilder()
                            .addProperty("cleaned", Boolean::class)
                            .build()
                    )
                    .addFunction(
                        FunSpec.builder("flywayMigrationStrategy")
                            .addAnnotation(Bean::class)
                            .returns(FlywayMigrationStrategy::class)
                            .addCode(
                                CodeBlock.of(
                                    """
                                        return FlywayMigrationStrategy { flyway ->
                                            if (!cleaned) {
                                                flyway.clean()
                                                cleaned = true
                                            }
                                            flyway.migrate()
                                        }
                                    """.trimIndent()
                                )
                            )
                            .build()
                    )
                    .build()
            )
            .build()
            .writeTo(directory)
    }

    private fun generateFlywayMigrationFile(context: Context) {
        val directory = getResourceDirectory(context)
        val file = File(directory.absolutePath + File.separator + "db" + File.separator + "migration" + File.separator + "V1_0__initial.sql")
        if (file.exists())
            return

        file.parentFile.mkdirs()
        Files.writeString(file.toPath(), "")
    }
}
