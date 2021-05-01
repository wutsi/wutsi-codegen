package com.wutsi.codegen.kotlin.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.OVERRIDE
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.security.SecurityApi
import com.wutsi.security.apikey.ApiKeyAuthenticationProvider
import com.wutsi.security.apikey.ApiKeyContext
import com.wutsi.security.apikey.ApiKeyProvider
import com.wutsi.security.apikey.ApiKeyRequestInterceptor
import com.wutsi.stream.EventStream
import com.wutsi.stream.EventSubscription
import com.wutsi.tracing.TracingRequestInterceptor
import io.swagger.v3.oas.models.OpenAPI
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import javax.servlet.Filter
import kotlin.reflect.KClass

class SecurityCodeGenerator : AbstractKotlinCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val api = KotlinMapper(context).toAPI(openAPI)
        if (!api.isSecured())
            return

        generateSpringSecurityConfiguration(context)
        generateApiKeyConfiguration(context)
    }

    private fun generateSpringSecurityConfiguration(context: Context) {
        val directory = getSourceDirectory(context)
        val classname = ClassName(context.basePackage + ".config", "SecurityConfiguration")

        System.out.println("Generating $classname to $directory")
        FileSpec.builder(classname.packageName, classname.simpleName)
            .addType(toSpringSecurityTypeSpec(classname))
            .build()
            .writeTo(getSourceDirectory(context))
    }

    private fun toSpringSecurityTypeSpec(className: ClassName): TypeSpec =
        TypeSpec.classBuilder(className)
            .addAnnotation(Configuration::class)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(toParameterSpec("apiKeyProvider", ApiKeyProvider::class))
                    .addParameter(toParameterSpec("apiKeyHeader", String::class, "api-key.header"))
                    .build()
            )
            .superclass(WebSecurityConfigurerAdapter::class)
            .addProperty(toPropertySpec("apiKeyProvider", ApiKeyProvider::class))
            .addProperty(toPropertySpec("apiKeyHeader", String::class))
            .addFunction(
                FunSpec.builder("configure")
                    .addModifiers(OVERRIDE)
                    .addParameter("http", HttpSecurity::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                http
                                    .csrf()
                                    .disable()
                                    .sessionManagement()
                                    .sessionCreationPolicy(
                                        org.springframework.security.config.http.SessionCreationPolicy.STATELESS
                                    )
                                    .and()
                                    .authorizeRequests().anyRequest().authenticated()
                                    .and()
                                    .addFilterBefore(authenticationFilter(), org.springframework.security.web.authentication.AnonymousAuthenticationFilter::class.java)
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("configure")
                    .addModifiers(OVERRIDE)
                    .addParameter("auth", AuthenticationManagerBuilder::class)
                    .addCode(
                        """
                            auth.authenticationProvider(apiKeyAuthenticationProvider())
                        """.trimIndent()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("apiKeyAuthenticationProvider")
                    .addModifiers(PRIVATE)
                    .returns(ApiKeyAuthenticationProvider::class)
                    .addCode(
                        """
                            return ApiKeyAuthenticationProvider()
                        """.trimIndent()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("authenticationFilter")
                    .returns(Filter::class)
                    .addCode(
                        """
                            val filter = com.wutsi.security.apikey.ApiKeyAuthenticationFilter(
                                headerName = apiKeyHeader,
                                apiProvider = apiKeyProvider,
                                pattern = "/**"
                            )
                            filter.setAuthenticationManager(authenticationManagerBean())
                            return filter
                        """.trimIndent()
                    )
                    .build()
            )
            .build()

    private fun generateApiKeyConfiguration(context: Context) {
        val directory = getSourceDirectory(context)
        val classname = ClassName(context.basePackage + ".config", "ApiKeyConfiguration")

        System.out.println("Generating $classname to $directory")
        FileSpec.builder(classname.packageName, classname.simpleName)
            .addType(toApiKeyTypeSpec(classname))
            .build()
            .writeTo(getSourceDirectory(context))
    }

    private fun toApiKeyTypeSpec(className: ClassName): TypeSpec {
        return TypeSpec.classBuilder(className)
            .addAnnotation(Configuration::class)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(toParameterSpec("context", ApplicationContext::class))
                    .addParameter(toParameterSpec("env", Environment::class))
                    .addParameter(toParameterSpec("mapper", ObjectMapper::class))
                    .addParameter(toParameterSpec("tracingRequestInterceptor", TracingRequestInterceptor::class))
                    .addParameter(toParameterSpec("eventStream", EventStream::class))
                    .addParameter(toParameterSpec("apiKeyId", String::class, "api-key.id"))
                    .addParameter(toParameterSpec("apiKeyHeader", String::class, "api-key.header"))
                    .build()
            )
            .addProperty(toPropertySpec("context", ApplicationContext::class))
            .addProperty(toPropertySpec("env", Environment::class))
            .addProperty(toPropertySpec("mapper", ObjectMapper::class))
            .addProperty(toPropertySpec("tracingRequestInterceptor", TracingRequestInterceptor::class))
            .addProperty(toPropertySpec("eventStream", EventStream::class))
            .addProperty(toPropertySpec("apiKeyId", String::class))
            .addProperty(toPropertySpec("apiKeyHeader", String::class))
            .addFunction(
                FunSpec.builder("apiKeyRequestInterceptor")
                    .addAnnotation(Bean::class)
                    .returns(ApiKeyRequestInterceptor::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                return ApiKeyRequestInterceptor(apiKeyContext())
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("apiKeyContext")
                    .addAnnotation(Bean::class)
                    .returns(ApiKeyContext::class)
                    .addCode(
                        """
                            return com.wutsi.security.apikey.DynamicApiKeyContext(
                                headerName = apiKeyHeader,
                                apiKeyId = apiKeyId,
                                context = context
                            )
                        """.trimIndent()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("apiKeyProvider")
                    .addAnnotation(Bean::class)
                    .returns(ApiKeyProvider::class)
                    .addCode(
                        """
                            return ApiKeyProvider(securityApi())
                        """.trimIndent()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("securitySubscription")
                    .addAnnotation(Bean::class)
                    .returns(EventSubscription::class)
                    .addCode(
                        """
                            return EventSubscription(com.wutsi.security.event.SecurityEventStream.NAME, eventStream)
                        """.trimIndent()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("securityEnvironment")
                    .returns(com.wutsi.security.Environment::class)
                    .addCode(
                        """
                            return if (env.acceptsProfiles(org.springframework.core.env.Profiles.of("prod")))
                                com.wutsi.security.Environment.PRODUCTION
                            else
                                com.wutsi.security.Environment.SANDBOX
                        """.trimIndent()
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("securityApi")
                    .addAnnotation(Bean::class)
                    .returns(SecurityApi::class)
                    .addCode(
                        """
                            return com.wutsi.security.SecurityApiBuilder()
                                .build(
                                    env = securityEnvironment(),
                                    mapper = mapper,
                                    interceptors = kotlin.collections.listOf(tracingRequestInterceptor, apiKeyRequestInterceptor())
                                )
                        """.trimIndent()
                    )
                    .build()
            )
            .build()
    }

    private fun toParameterSpec(name: String, type: KClass<*>, valueName: String? = null): ParameterSpec {
        val builder = ParameterSpec.builder(name, type)
            .addModifiers(PRIVATE)

        if (valueName == null) {
            builder.addAnnotation(
                AnnotationSpec.builder(Autowired::class)
                    .build()
            )
        } else {
            builder.addAnnotation(
                AnnotationSpec.builder(Value::class)
                    .addMember("value=\"\\\${security.$valueName}\"")
                    .build()
            )
        }

        return builder.build()
    }

    private fun toPropertySpec(name: String, type: KClass<*>): PropertySpec =
        PropertySpec.builder(name, type)
            .initializer(name)
            .addModifiers(PRIVATE)
            .build()
}
