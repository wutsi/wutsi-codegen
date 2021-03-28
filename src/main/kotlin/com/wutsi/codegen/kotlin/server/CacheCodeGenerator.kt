package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import net.rubyeye.xmemcached.MemcachedClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import kotlin.reflect.KClass

class CacheCodeGenerator : AbstractKotlinCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        generateLocalConfiguration(context)
        generateRemoteConfiguration(context)
    }

    private fun generateLocalConfiguration(context: Context) {
        val directory = getSourceDirectory(context)
        val classname = ClassName(context.basePackage + ".config", "CacheLocalConfiguration")

        System.out.println("Generating $classname to $directory")
        FileSpec.builder(classname.packageName, classname.simpleName)
            .addType(toLocalTypeSpec(classname, context))
            .build()
            .writeTo(getSourceDirectory(context))
    }

    private fun generateRemoteConfiguration(context: Context) {
        val directory = getSourceDirectory(context)
        val classname = ClassName(context.basePackage + ".config", "CacheRemoteConfiguration")

        System.out.println("Generating $classname to $directory")
        FileSpec.builder(classname.packageName, classname.simpleName)
            .addType(toRemoteTypeSpec(classname, context))
            .build()
            .writeTo(getSourceDirectory(context))
    }

    private fun toLocalTypeSpec(className: ClassName, context: Context): TypeSpec =
        TypeSpec.classBuilder(className)
            .addAnnotation(Configuration::class)
            .addAnnotation(
                AnnotationSpec.builder(ConditionalOnProperty::class)
                    .addMember("value=[%S]", "memcached.enabled")
                    .addMember("havingValue = %S", "false")
                    .build()
            )
            .addFunction(
                FunSpec.builder("cacheManager")
                    .addAnnotation(Bean::class)
                    .returns(CacheManager::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                val cacheManager = org.springframework.cache.support.SimpleCacheManager()
                                cacheManager.setCaches(
                                    listOf(
                                        org.springframework.cache.concurrent.ConcurrentMapCache("default", true)
                                    )
                                )
                                return cacheManager
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .build()

    private fun toRemoteTypeSpec(className: ClassName, context: Context): TypeSpec =
        TypeSpec.classBuilder(className)
            .addAnnotation(Configuration::class)
            .addAnnotation(
                AnnotationSpec.builder(ConditionalOnProperty::class)
                    .addMember("value=[%S]", "memcached.enabled")
                    .addMember("havingValue = %S", "true")
                    .build()
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(toParameterSpec("username", String::class))
                    .addParameter(toParameterSpec("password", String::class))
                    .addParameter(toParameterSpec("servers", String::class))
                    .addParameter(toParameterSpec("ttl", Int::class))
                    .build()
            )
            .addProperty(toPropertyStep("username", String::class))
            .addProperty(toPropertyStep("password", String::class))
            .addProperty(toPropertyStep("servers", String::class))
            .addProperty(toPropertyStep("ttl", Int::class))
            .addFunction(
                FunSpec.builder("memcachedClient")
                    .addAnnotation(Bean::class)
                    .returns(MemcachedClient::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                return com.wutsi.spring.memcached.MemcachedClientBuilder()
                                    .withServers(servers)
                                    .withPassword(password)
                                    .withUsername(username)
                                    .build()
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("cacheManager")
                    .addAnnotation(Bean::class)
                    .returns(CacheManager::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                val cacheManager = org.springframework.cache.support.SimpleCacheManager()
                                cacheManager.setCaches(
                                    listOf(
                                        com.wutsi.spring.memcached.MemcachedCache("default", ttl, memcachedClient())
                                    )
                                )
                                return cacheManager
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("memcachedHealthIndicator")
                    .addAnnotation(Bean::class)
                    .returns(HealthIndicator::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                return com.wutsi.spring.memcached.MemcachedHealthIndicator(memcachedClient())
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .build()

    private fun toParameterSpec(name: String, type: KClass<*>): ParameterSpec =
        ParameterSpec.builder(name, type)
            .addAnnotation(
                AnnotationSpec.builder(Value::class)
                    .addMember("value=\"\\\${memcached.$name}\"")
                    .build()
            )
            .addModifiers(PRIVATE)
            .build()

    private fun toPropertyStep(name: String, type: KClass<*>): PropertySpec =
        PropertySpec.builder(name, type)
            .initializer(name)
            .addModifiers(PRIVATE)
            .build()
}
