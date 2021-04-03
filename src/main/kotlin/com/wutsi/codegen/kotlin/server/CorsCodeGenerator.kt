package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import org.springframework.stereotype.Component
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

class CorsCodeGenerator : AbstractKotlinCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val file = getSourceDirectory(context)
        val packageName = context.basePackage + ".servlet"
        val classname = "CorsFilter"

        System.out.println("Generating $packageName.$classname to $file")
        FileSpec.builder(packageName, classname)
            .addType(
                TypeSpec.classBuilder(ClassName(packageName, classname))
                    .addAnnotation(Component::class)
                    .addSuperinterface(Filter::class)
                    .addFunction(
                        FunSpec.builder("doFilter")
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameter(ParameterSpec.builder("req", ServletRequest::class).build())
                            .addParameter(ParameterSpec.builder("resp", ServletResponse::class).build())
                            .addParameter(ParameterSpec.builder("chain", FilterChain::class).build())
                            .addCode(code())
                            .build()
                    )
                    .build()
            )
            .build()
            .writeTo(file)
    }

    fun code() = CodeBlock.of(
        """
            (resp as javax.servlet.http.HttpServletResponse).addHeader(
                "Access-Control-Allow-Origin",
                "*"
            )
            resp.addHeader(
                "Access-Control-Allow-Methods",
                "GET, OPTIONS, HEAD, PUT, POST, DELETE"
            )
            resp.addHeader(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization, Content-Length,X-Requested-With"
            )
            chain.doFilter(req, resp)
        """.trimIndent()
    )
}
