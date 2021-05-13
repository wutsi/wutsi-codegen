package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Api
import com.wutsi.platform.EnableWutsiCore
import com.wutsi.platform.EnableWutsiSecurity
import com.wutsi.platform.EnableWutsiSite
import io.swagger.v3.oas.models.OpenAPI
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.io.File

class ServerLauncherCodeGenerator : AbstractKotlinCodeGenerator() {
    companion object {
        const val CLASSNAME = "Application"
    }

    override fun generate(openAPI: OpenAPI, context: Context) {
        val mapper = KotlinMapper(context)
        val api = mapper.toAPI(openAPI)
        generateClass(api, context)
    }

    private fun generateClass(api: Api, context: Context) {
        val directory = getSourceDirectory(context)
        val classname = ClassName(context.basePackage, CLASSNAME)
        val relativePath = classname.toString().replace('.', File.separatorChar)
        if (File(directory.absolutePath + File.separator + relativePath + ".kt").exists())
            return

        System.out.println("Generating $classname to $directory")
        FileSpec.builder(classname.packageName, classname.simpleName)
            .addType(toTypeSpec(api, context))
            .addFunction(toFunSpec())
            .build()
            .writeTo(getSourceDirectory(context))
    }

    private fun toTypeSpec(api: Api, context: Context): TypeSpec {
        val spec = TypeSpec.classBuilder(ClassName(context.basePackage, CLASSNAME))
            .addAnnotation(SpringBootApplication::class)
            .addAnnotation(EnableAsync::class)
            .addAnnotation(EnableWutsiCore::class)

        if (context.hasService(Context.SERVICE_DATABASE))
            spec.addAnnotation(EnableTransactionManagement::class.java)
        if (context.hasService(Context.SERVICE_CACHE))
            spec.addAnnotation(EnableCaching::class.java)
        if (context.hasService(Context.SERVICE_MQUEUE))
            spec.addAnnotation(EnableScheduling::class.java)
        if (api.isSecured()) {
            spec.addAnnotation(EnableWebSecurity::class.java)
            spec.addAnnotation(
                AnnotationSpec.builder(EnableGlobalMethodSecurity::class.java)
                    .addMember("prePostEnabled = true")
                    .build()
            )
            if (api.name != "security")
                spec.addAnnotation(EnableWutsiSecurity::class.java)
        }
        if (api.name != "site")
            spec.addAnnotation(EnableWutsiSite::class.java)

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
