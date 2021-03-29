package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.FileSystemUtils
import java.io.File

internal class CacheCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    val codegen = CacheCodeGenerator()

    @BeforeEach
    fun setUp() {
        FileSystemUtils.deleteRecursively(File(context.outputDirectory))
    }

    @Test
    fun localConfiguration() {
        val openAPI = createOpenAPI()
        context.addService(Context.SERVICE_CACHE)

        codegen.generate(openAPI, context)

        val file = File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/config/CacheLocalConfiguration.kt")
        kotlin.test.assertTrue(file.exists())

        val text = file.readText()
        kotlin.test.assertEquals(
            """
                package com.wutsi.test.config

                import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
                import org.springframework.cache.CacheManager
                import org.springframework.context.`annotation`.Bean
                import org.springframework.context.`annotation`.Configuration

                @Configuration
                @ConditionalOnProperty(
                  value=["memcached.enabled"],
                  havingValue = "false"
                )
                public class CacheLocalConfiguration {
                  @Bean
                  public fun cacheManager(): CacheManager {
                    val cacheManager = org.springframework.cache.support.SimpleCacheManager()
                    cacheManager.setCaches(
                        listOf(
                            org.springframework.cache.concurrent.ConcurrentMapCache("default", true)
                        )
                    )
                    return cacheManager
                  }
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }

    @Test
    fun remoteConfiguration() {
        val openAPI = createOpenAPI()
        context.addService(Context.SERVICE_CACHE)

        codegen.generate(openAPI, context)

        val file = File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/config/CacheRemoteConfiguration.kt")
        kotlin.test.assertTrue(file.exists())

        val text = file.readText()
        kotlin.test.assertEquals(
            """
                package com.wutsi.test.config

                import kotlin.Int
                import kotlin.String
                import net.rubyeye.xmemcached.MemcachedClient
                import org.springframework.beans.factory.`annotation`.Value
                import org.springframework.boot.actuate.health.HealthIndicator
                import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
                import org.springframework.cache.CacheManager
                import org.springframework.context.`annotation`.Bean
                import org.springframework.context.`annotation`.Configuration

                @Configuration
                @ConditionalOnProperty(
                  value=["memcached.enabled"],
                  havingValue = "true"
                )
                public class CacheRemoteConfiguration(
                  @Value(value="\${'$'}{memcached.username}")
                  private val username: String,
                  @Value(value="\${'$'}{memcached.password}")
                  private val password: String,
                  @Value(value="\${'$'}{memcached.servers}")
                  private val servers: String,
                  @Value(value="\${'$'}{memcached.ttl}")
                  private val ttl: Int
                ) {
                  @Bean
                  public fun memcachedClient(): MemcachedClient =
                      com.wutsi.spring.memcached.MemcachedClientBuilder()
                      .withServers(servers)
                      .withPassword(password)
                      .withUsername(username)
                      .build()

                  @Bean
                  public fun cacheManager(): CacheManager {
                    val cacheManager = org.springframework.cache.support.SimpleCacheManager()
                    cacheManager.setCaches(
                        listOf(
                            com.wutsi.spring.memcached.MemcachedCache("default", ttl, memcachedClient())
                        )
                    )
                    return cacheManager
                  }

                  @Bean
                  public fun memcachedHealthIndicator(): HealthIndicator =
                      com.wutsi.spring.memcached.MemcachedHealthIndicator(memcachedClient())
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }

    private fun createOpenAPI(): OpenAPI {
        val openAPI = OpenAPI()
        openAPI.info = Info()
        openAPI.info.version = "1.3.7"
        return openAPI
    }
}
