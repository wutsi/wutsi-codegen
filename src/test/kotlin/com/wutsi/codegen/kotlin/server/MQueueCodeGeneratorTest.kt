package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.FileSystemUtils
import java.io.File
import kotlin.test.assertFalse

internal class MQueueCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    val codegen = MQueueCodeGenerator()

    @BeforeEach
    fun setUp() {
        FileSystemUtils.deleteRecursively(File(context.outputDirectory))
    }

    @Test
    fun localConfiguration() {
        val openAPI = createOpenAPI()
        context.addService(Context.SERVICE_MQUEUE)

        codegen.generate(openAPI, context)

        val file = File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/config/MQueueLocalConfiguration.kt")
        kotlin.test.assertTrue(file.exists())

        val text = file.readText()
        kotlin.test.assertEquals(
            """
            package com.wutsi.test.config

            import com.wutsi.stream.EventStream
            import org.springframework.beans.factory.`annotation`.Autowired
            import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
            import org.springframework.context.ApplicationEventPublisher
            import org.springframework.context.`annotation`.Bean
            import org.springframework.context.`annotation`.Configuration

            @Configuration
            @ConditionalOnProperty(
              value=["rabbitmq.enabled"],
              havingValue = "false"
            )
            public class MQueueLocalConfiguration(
              @Autowired
              private val eventPublisher: ApplicationEventPublisher
            ) {
              @Bean
              public fun eventStream(): EventStream = com.wutsi.stream.file.FileEventStream(
                  name = "test",
                  root = java.io.File(System.getProperty("user.home") + java.io.File.separator + "tmp",
                  "mqueue"),
                  handler = object : com.wutsi.stream.EventHandler {
                      override fun onEvent(event: com.wutsi.stream.Event) {
                          eventPublisher.publishEvent(event)
                      }
                  }
              )
            }
            """.trimIndent(),
            text.trimIndent()
        )
    }

    @Test
    fun remoteConfiguration() {
        val openAPI = createOpenAPI()
        context.addService(Context.SERVICE_MQUEUE)

        codegen.generate(openAPI, context)

        val file = File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/config/MQueueRemoteConfiguration.kt")
        kotlin.test.assertTrue(file.exists())

        val text = file.readText()
        kotlin.test.assertEquals(
            """
            package com.wutsi.test.config

            import com.rabbitmq.client.Channel
            import com.rabbitmq.client.ConnectionFactory
            import com.wutsi.stream.rabbitmq.RabbitMQEventStream
            import java.util.concurrent.ExecutorService
            import kotlin.Int
            import kotlin.String
            import org.springframework.beans.factory.`annotation`.Autowired
            import org.springframework.beans.factory.`annotation`.Value
            import org.springframework.boot.actuate.health.HealthIndicator
            import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
            import org.springframework.context.ApplicationEventPublisher
            import org.springframework.context.`annotation`.Bean
            import org.springframework.context.`annotation`.Configuration

            @Configuration
            @ConditionalOnProperty(
              value=["rabbitmq.enabled"],
              havingValue = "true"
            )
            public class MQueueRemoteConfiguration(
              @Autowired
              private val eventPublisher: ApplicationEventPublisher,
              @Value(value="\${'$'}{rabbitmq.url}")
              private val url: String,
              @Value(value="\${'$'}{rabbitmq.thread-pool-size}")
              private val threadPoolSize: Int
            ) {
              @Bean
              public fun connectionFactory(): ConnectionFactory {
                val factory = ConnectionFactory()
                factory.setUri(url)
                return factory
              }

              @Bean(destroyMethod="shutdown")
              public fun executorService(): ExecutorService =
                  java.util.concurrent.Executors.newFixedThreadPool(threadPoolSize)

              @Bean(destroyMethod="close")
              public fun channel(): Channel = connectionFactory()
                  .newConnection(executorService())
                  .createChannel()

              @Bean(destroyMethod="close")
              public fun eventStream(): RabbitMQEventStream = com.wutsi.stream.rabbitmq.RabbitMQEventStream(
                  name = "test",
                  channel = channel(),
                  handler = object : com.wutsi.stream.EventHandler {
                      override fun onEvent(event: com.wutsi.stream.Event) {
                          eventPublisher.publishEvent(event)
                      }
                  }
              )

              @Bean
              public fun rabbitMQHealthIndicator(): HealthIndicator =
                  com.wutsi.stream.rabbitmq.RabbitMQHealthIndicator(channel())
            }
            """.trimIndent(),
            text.trimIndent()
        )
    }

    @Test
    private fun `do not generate files when service not enabled`() {
        val openAPI = createOpenAPI()

        codegen.generate(openAPI, context)

        assertFalse(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/config/MQueueRemoteConfiguration.kt").exists())
        assertFalse(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/config/MQueueLocalConfiguration.kt").exists())
    }

    private fun createOpenAPI(): OpenAPI {
        val openAPI = OpenAPI()
        openAPI.info = Info()
        openAPI.info.version = "1.3.7"
        return openAPI
    }
}
