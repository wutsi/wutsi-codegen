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

                import com.wutsi.platform.security.apikey.ApiKeyAuthenticationProvider
                import com.wutsi.platform.security.apikey.ApiKeyProvider
                import javax.servlet.Filter
                import kotlin.Unit
                import org.springframework.beans.factory.`annotation`.Autowired
                import org.springframework.context.`annotation`.Configuration
                import org.springframework.security.config.`annotation`.authentication.builders.AuthenticationManagerBuilder
                import org.springframework.security.config.`annotation`.web.builders.HttpSecurity
                import org.springframework.security.config.`annotation`.web.configuration.WebSecurityConfigurerAdapter
                import org.springframework.security.web.util.matcher.RequestMatcher

                @Configuration
                public class SecurityConfiguration(
                  @Autowired
                  private val apiKeyProvider: ApiKeyProvider
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
                        .authorizeRequests()
                        .requestMatchers(SECURED_ENDPOINTS).authenticated()
                        .anyRequest().permitAll()
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
                    val filter = com.wutsi.platform.security.apikey.ApiKeyAuthenticationFilter(
                        apiProvider = apiKeyProvider,
                        requestMatcher = SECURED_ENDPOINTS
                    )
                    filter.setAuthenticationManager(authenticationManagerBean())
                    return filter
                  }

                  public companion object {
                    public val SECURED_ENDPOINTS: RequestMatcher =
                        org.springframework.security.web.util.matcher.OrRequestMatcher(
                        org.springframework.security.web.util.matcher.AntPathRequestMatcher("/v1/likes","POST"),
                        org.springframework.security.web.util.matcher.AntPathRequestMatcher("/v1/likes/stats","GET"),
                        org.springframework.security.web.util.matcher.AntPathRequestMatcher("/v1/likes/*","DELETE")
                        )
                  }
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
