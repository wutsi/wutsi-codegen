package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.core.util.CaseUtil
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import com.wutsi.tracing.RequestTracingContext
import com.wutsi.tracing.TracingContextProvider
import feign.RequestInterceptor
import io.swagger.v3.oas.models.OpenAPI
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.servlet.Filter
import javax.servlet.http.HttpServletRequest
import kotlin.reflect.KClass

class TracingCodeGenerator : AbstractKotlinCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val directory = getSourceDirectory(context)
        val classname = ClassName(context.basePackage + ".config", "TracingConfiguration")

        System.out.println("Generating $classname to $directory")
        FileSpec.builder(classname.packageName, classname.simpleName)
            .addType(toTypeSpec(classname, context))
            .build()
            .writeTo(getSourceDirectory(context))
    }

    private fun toTypeSpec(className: ClassName, context: Context): TypeSpec {
        val clientId = CaseUtil.toSnakeCase("${context.apiName}-server").toLowerCase()
        return TypeSpec.classBuilder(className)
            .addAnnotation(Configuration::class)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(toParameterSpec("request", HttpServletRequest::class))
                    .addParameter(toParameterSpec("context", ApplicationContext::class))
                    .build()
            )
            .addProperty(toPropertyStep("request", HttpServletRequest::class))
            .addProperty(toPropertyStep("context", ApplicationContext::class))
            .addFunction(
                FunSpec.builder("tracingFilter")
                    .addAnnotation(Bean::class)
                    .returns(Filter::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                return com.wutsi.tracing.TracingFilter(tracingContextProvider())
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("requestTracingContext")
                    .addAnnotation(Bean::class)
                    .returns(RequestTracingContext::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                return RequestTracingContext(request)
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("tracingContextProvider")
                    .addAnnotation(Bean::class)
                    .returns(TracingContextProvider::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                return TracingContextProvider(context)
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("tracingRequestInterceptor")
                    .addAnnotation(Bean::class)
                    .returns(RequestInterceptor::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                return com.wutsi.tracing.TracingRequestInterceptor("$clientId", tracingContextProvider())
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .build()
    }

    private fun toParameterSpec(name: String, type: KClass<*>): ParameterSpec =
        ParameterSpec.builder(name, type)
            .addAnnotation(Autowired::class)
            .addModifiers(PRIVATE)
            .build()

    private fun toPropertyStep(name: String, type: KClass<*>): PropertySpec =
        PropertySpec.builder(name, type)
            .initializer(name)
            .addModifiers(PRIVATE)
            .build()
}
