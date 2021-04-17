package com.wutsi.codegen.kotlin.server

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
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
import com.wutsi.codegen.core.util.CaseUtil
import com.wutsi.codegen.kotlin.AbstractKotlinCodeGenerator
import com.wutsi.stream.EventStream
import com.wutsi.tracing.TracingContext
import io.swagger.v3.oas.models.OpenAPI
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.ExecutorService
import kotlin.reflect.KClass

class MQueueCodeGenerator : AbstractKotlinCodeGenerator() {
    override fun generate(openAPI: OpenAPI, context: Context) {
        if (!context.hasService(Context.SERVICE_MQUEUE))
            return

        generateLocalConfiguration(context)
        generateRemoteConfiguration(context)
    }

    private fun generateLocalConfiguration(context: Context) {
        val directory = getSourceDirectory(context)
        val classname = ClassName(context.basePackage + ".config", "MQueueLocalConfiguration")

        System.out.println("Generating $classname to $directory")
        FileSpec.builder(classname.packageName, classname.simpleName)
            .addType(toLocalTypeSpec(classname, context))
            .build()
            .writeTo(getSourceDirectory(context))
    }

    private fun generateRemoteConfiguration(context: Context) {
        val directory = getSourceDirectory(context)
        val classname = ClassName(context.basePackage + ".config", "MQueueRemoteConfiguration")

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
                    .addMember("value=[%S]", "rabbitmq.enabled")
                    .addMember("havingValue = %S", "false")
                    .build()
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(toParameterSpec("eventPublisher", ApplicationEventPublisher::class))
                    .build()
            )
            .addProperty(toPropertySpec("eventPublisher", ApplicationEventPublisher::class))
            .addFunction(
                FunSpec.builder("eventStream")
                    .addAnnotation(Bean::class)
                    .returns(EventStream::class)
                    .addCode(
                        CodeBlock.of(
                            """
                            return com.wutsi.stream.file.FileEventStream(
                                name = "${CaseUtil.toCamelCase(context.apiName, false)}",
                                root = java.io.File(System.getProperty("user.home") + java.io.File.separator + "tmp", "mqueue"),
                                handler = object : com.wutsi.stream.EventHandler {
                                    override fun onEvent(event: com.wutsi.stream.Event) {
                                        eventPublisher.publishEvent(event)
                                    }
                                }
                            )
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
                    .addMember("value=[%S]", "rabbitmq.enabled")
                    .addMember("havingValue = %S", "true")
                    .build()
            )
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(toParameterSpec("tracingContext", TracingContext::class))
                    .addParameter(toParameterSpec("eventPublisher", ApplicationEventPublisher::class))
                    .addParameter(toParameterSpec("url", String::class, "url"))
                    .addParameter(toParameterSpec("threadPoolSize", Int::class, "thread-pool-size"))
                    .addParameter(toParameterSpec("maxRetries", Int::class, "max-retries"))
                    .addParameter(toParameterSpec("queueTtlSeconds", Long::class, "queue-ttl-seconds"))
                    .build()
            )
            .addProperty(toPropertySpec("tracingContext", TracingContext::class))
            .addProperty(toPropertySpec("eventPublisher", ApplicationEventPublisher::class))
            .addProperty(toPropertySpec("url", String::class))
            .addProperty(toPropertySpec("threadPoolSize", Int::class))
            .addProperty(toPropertySpec("maxRetries", Int::class))
            .addProperty(toPropertySpec("queueTtlSeconds", Long::class))
            .addFunction(
                FunSpec.builder("connectionFactory")
                    .addAnnotation(Bean::class)
                    .returns(ConnectionFactory::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                val factory = ConnectionFactory()
                                factory.setUri(url)
                                return factory
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("executorService")
                    .addAnnotation(
                        AnnotationSpec.builder(Bean::class)
                            .addMember("destroyMethod=%S", "shutdown")
                            .build()
                    )
                    .returns(ExecutorService::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                return java.util.concurrent.Executors.newFixedThreadPool(threadPoolSize)
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("channel")
                    .addAnnotation(
                        AnnotationSpec.builder(Bean::class)
                            .addMember("destroyMethod=%S", "close")
                            .build()
                    )
                    .returns(Channel::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                return connectionFactory()
                                    .newConnection(executorService())
                                    .createChannel()
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("eventStream")
                    .addAnnotation(
                        AnnotationSpec.builder(Bean::class)
                            .addMember("destroyMethod=%S", "close")
                            .build()
                    )
                    .returns(EventStream::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                return com.wutsi.stream.rabbitmq.RabbitMQEventStream(
                                    name = "${CaseUtil.toCamelCase(context.apiName, false)}",
                                    channel = channel(),
                                    queueTtlSeconds = queueTtlSeconds,
                                    maxRetries = maxRetries,
                                    handler = object : com.wutsi.stream.EventHandler {
                                        override fun onEvent(event: com.wutsi.stream.Event) {
                                            com.wutsi.tracing.TracingMDCHelper.initMDC(tracingContext)
                                            eventPublisher.publishEvent(event)
                                        }
                                    }
                                )
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("rabbitMQHealthIndicator")
                    .addAnnotation(Bean::class)
                    .returns(HealthIndicator::class)
                    .addCode(
                        CodeBlock.of(
                            """
                                return com.wutsi.stream.rabbitmq.RabbitMQHealthIndicator(channel())
                            """.trimIndent()
                        )
                    )
                    .build()
            )
            .addFunction(
                FunSpec.builder("replayDlq")
                    .addAnnotation(
                        AnnotationSpec.builder(Scheduled::class)
                            .addMember("cron=\"\\\${rabbitmq.replay-cron}\"")
                            .build()
                    )
                    .addCode(
                        CodeBlock.of(
                            """
                                (eventStream() as com.wutsi.stream.rabbitmq.RabbitMQEventStream).replayDlq()
                            """.trimIndent()
                        )
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
                    .addMember("value=\"\\\${rabbitmq.$valueName}\"")
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
