package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.sdk.SdkCodeGenerator
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.FileSystemUtils
import java.io.File
import kotlin.test.assertTrue

internal class SecurityCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    val codegen = SecurityCodeGenerator()

    @BeforeEach
    fun setUp() {
        FileSystemUtils.deleteRecursively(File(context.outputDirectory))
    }

    @Test
    fun securityConfiguration() {
        val openAPI = createOpenAPI()
        codegen.generate(openAPI, context)

        val file = File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/config/SecurityConfiguration.kt")
        assertTrue(file.exists())

        val text = file.readText()
        kotlin.test.assertEquals(
            """
                package com.wutsi.test.config

                import com.wutsi.security.apikey.ApiKeyAuthenticationProvider
                import com.wutsi.security.apikey.ApiKeyProvider
                import javax.servlet.Filter
                import kotlin.String
                import kotlin.Unit
                import org.springframework.beans.factory.`annotation`.Autowired
                import org.springframework.beans.factory.`annotation`.Value
                import org.springframework.context.`annotation`.Configuration
                import org.springframework.security.config.`annotation`.authentication.builders.AuthenticationManagerBuilder
                import org.springframework.security.config.`annotation`.web.builders.HttpSecurity
                import org.springframework.security.config.`annotation`.web.configuration.WebSecurityConfigurerAdapter

                @Configuration
                public class SecurityConfiguration(
                  @Autowired
                  private val apiKeyProvider: ApiKeyProvider,
                  @Value(value="\${'$'}{security.api-key.header}")
                  private val apiKeyHeader: String
                ) : WebSecurityConfigurerAdapter() {
                  public override fun configure(http: HttpSecurity): Unit {
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
                        .addFilterBefore(authenticationFilter(),
                        org.springframework.security.web.authentication.AnonymousAuthenticationFilter::class.java)
                  }

                  public override fun configure(auth: AuthenticationManagerBuilder): Unit {
                    auth.authenticationProvider(apiKeyAuthenticationProvider())
                  }

                  private fun apiKeyAuthenticationProvider(): ApiKeyAuthenticationProvider =
                      ApiKeyAuthenticationProvider()

                  public fun authenticationFilter(): Filter {
                    val filter = com.wutsi.security.apikey.ApiKeyAuthenticationFilter(
                        headerName = apiKeyHeader,
                        apiProvider = apiKeyProvider,
                        pattern = "/**"
                    )
                    filter.setAuthenticationManager(authenticationManagerBean())
                    return filter
                  }
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }

    @Test
    fun apiKeyConfiguration() {
        val openAPI = createOpenAPI()
        codegen.generate(openAPI, context)

        val file = File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/config/ApiKeyConfiguration.kt")
        assertTrue(file.exists())

        val text = file.readText()
        kotlin.test.assertEquals(
            """
                package com.wutsi.test.config

                import com.fasterxml.jackson.databind.ObjectMapper
                import com.wutsi.security.SecurityApi
                import com.wutsi.security.apikey.ApiKeyContext
                import com.wutsi.security.apikey.ApiKeyProvider
                import com.wutsi.security.apikey.ApiKeyRequestInterceptor
                import com.wutsi.stream.EventStream
                import com.wutsi.stream.EventSubscription
                import com.wutsi.tracing.TracingRequestInterceptor
                import kotlin.String
                import org.springframework.beans.factory.`annotation`.Autowired
                import org.springframework.beans.factory.`annotation`.Value
                import org.springframework.context.ApplicationContext
                import org.springframework.context.`annotation`.Bean
                import org.springframework.context.`annotation`.Configuration
                import org.springframework.core.env.Environment

                @Configuration
                public class ApiKeyConfiguration(
                  @Autowired
                  private val context: ApplicationContext,
                  @Autowired
                  private val env: Environment,
                  @Autowired
                  private val mapper: ObjectMapper,
                  @Autowired
                  private val tracingRequestInterceptor: TracingRequestInterceptor,
                  @Autowired
                  private val eventStream: EventStream,
                  @Value(value="\${'$'}{security.api-key.id}")
                  private val apiKeyId: String,
                  @Value(value="\${'$'}{security.api-key.header}")
                  private val apiKeyHeader: String
                ) {
                  @Bean
                  public fun apiKeyRequestInterceptor(): ApiKeyRequestInterceptor =
                      ApiKeyRequestInterceptor(apiKeyContext())

                  @Bean
                  public fun apiKeyContext(): ApiKeyContext = com.wutsi.security.apikey.DynamicApiKeyContext(
                      headerName = apiKeyHeader,
                      apiKeyId = apiKeyId,
                      context = context
                  )

                  @Bean
                  public fun apiKeyProvider(): ApiKeyProvider = ApiKeyProvider(securityApi())

                  @Bean
                  public fun securitySubscription(): EventSubscription =
                      EventSubscription(com.wutsi.security.event.SecurityEventStream.NAME, eventStream)

                  public fun securityEnvironment(): com.wutsi.security.Environment = if
                      (env.acceptsProfiles(org.springframework.core.env.Profiles.of("prod")))
                      com.wutsi.security.Environment.PRODUCTION
                  else
                      com.wutsi.security.Environment.SANDBOX

                  @Bean
                  public fun securityApi(): SecurityApi = com.wutsi.security.SecurityApiBuilder()
                      .build(
                          env = securityEnvironment(),
                          mapper = mapper,
                          interceptors = kotlin.collections.listOf(tracingRequestInterceptor,
                      apiKeyRequestInterceptor())
                      )
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }

    private fun createOpenAPI(): OpenAPI {
        val yaml = IOUtils.toString(SdkCodeGenerator::class.java.getResourceAsStream("/api.yaml"), "utf-8")
        return OpenAPIV3Parser().readContents(yaml).openAPI
    }
}
