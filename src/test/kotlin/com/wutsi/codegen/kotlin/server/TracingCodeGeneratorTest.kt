package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.FileSystemUtils
import java.io.File

internal class TracingCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    val codegen = TracingCodeGenerator()

    @BeforeEach
    fun setUp() {
        FileSystemUtils.deleteRecursively(File(context.outputDirectory))
    }

    @Test
    fun generate() {
        val openAPI = OpenAPI()

        codegen.generate(openAPI, context)

        val file = File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/config/TracingConfiguration.kt")
        kotlin.test.assertTrue(file.exists())

        val text = file.readText()
        kotlin.test.assertEquals(
            """
                package com.wutsi.test.config

                import com.wutsi.tracing.RequestTracingContext
                import com.wutsi.tracing.TracingContextProvider
                import feign.RequestInterceptor
                import javax.servlet.Filter
                import javax.servlet.http.HttpServletRequest
                import org.springframework.beans.factory.`annotation`.Autowired
                import org.springframework.context.ApplicationContext
                import org.springframework.context.`annotation`.Bean
                import org.springframework.context.`annotation`.Configuration

                @Configuration
                public class TracingConfiguration(
                  @Autowired
                  private val request: HttpServletRequest,
                  @Autowired
                  private val context: ApplicationContext
                ) {
                  @Bean
                  public fun tracingFilter(): Filter = com.wutsi.tracing.TracingFilter(tracingContextProvider())

                  @Bean
                  public fun requestTracingContext(): RequestTracingContext = RequestTracingContext(request)

                  @Bean
                  public fun tracingContextProvider(): TracingContextProvider = TracingContextProvider(context)

                  @Bean
                  public fun tracingRequestInterceptor(): RequestInterceptor =
                      com.wutsi.tracing.TracingRequestInterceptor("test-server", tracingContextProvider())
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }
}
