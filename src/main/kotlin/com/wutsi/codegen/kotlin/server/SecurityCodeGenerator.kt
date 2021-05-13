package com.wutsi.codegen.kotlin.server

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
import com.wutsi.codegen.model.Api
import com.wutsi.codegen.model.Endpoint
import com.wutsi.platform.security.apikey.ApiKeyAuthenticationProvider
import com.wutsi.platform.security.apikey.ApiKeyProvider
import io.swagger.v3.oas.models.OpenAPI
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.util.matcher.RequestMatcher
import javax.servlet.Filter
import kotlin.reflect.KClass

class SecurityCodeGenerator : AbstractKotlinCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        val api = KotlinMapper(context).toAPI(openAPI)
        if (!api.isSecured())
            return

        generateSpringSecurityConfiguration(api, context)
    }

    private fun generateSpringSecurityConfiguration(api: Api, context: Context) {
        val directory = getSourceDirectory(context)
        val classname = ClassName(context.basePackage + ".config", "SecurityConfiguration")

        System.out.println("Generating $classname to $directory")
        FileSpec.builder(classname.packageName, classname.simpleName)
            .addType(toSpringSecurityTypeSpec(api, classname))
            .build()
            .writeTo(getSourceDirectory(context))
    }

    private fun toRequestMather(api: Api): String {
        val items = api.endpoints
            .filter { it.isSecured() }
            .map { "org.springframework.security.web.util.matcher.AntPathRequestMatcher(\"${toAntPath(it)}\",\"${it.method}\")" }
            .toSet()
        return "org.springframework.security.web.util.matcher.OrRequestMatcher(\n" +
            items.joinToString(",\n") +
            "\n)"
    }

    private fun toAntPath(endpoint: Endpoint): String {
        val tokens = endpoint.path.split("/")
        if (tokens.isEmpty())
            return endpoint.path
        else {
            val xtokens = tokens
                .filter { it.isNotEmpty() }
                .map { if (it.startsWith("{")) "*" else it }
            return "/" + xtokens.joinToString(separator = "/")
        }

        endpoint.path.replace("(\\{.+})".toRegex(), "*")
    }

    private fun toSpringSecurityTypeSpec(api: Api, className: ClassName): TypeSpec =
        TypeSpec.classBuilder(className)
            .superclass(WebSecurityConfigurerAdapter::class)
            .addAnnotation(Configuration::class)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(toParameterSpec("apiKeyProvider", ApiKeyProvider::class))
                    .build()
            )
            .addProperty(toPropertySpec("apiKeyProvider", ApiKeyProvider::class))
            .addType(
                TypeSpec.companionObjectBuilder()
                    .addProperty(
                        PropertySpec.builder("SECURED_ENDPOINTS", RequestMatcher::class)
                            .initializer(toRequestMather(api))
                            .build()
                    )
                    .build()
            )
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
                                    .authorizeRequests()
                                    .requestMatchers(SECURED_ENDPOINTS).authenticated()
                                    .anyRequest().permitAll()
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
                            val filter = com.wutsi.platform.security.apikey.ApiKeyAuthenticationFilter(
                                apiProvider = apiKeyProvider,
                                requestMatcher = SECURED_ENDPOINTS
                            )
                            filter.setAuthenticationManager(authenticationManagerBean())
                            return filter
                        """.trimIndent()
                    )
                    .build()
            )
            .build()

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
