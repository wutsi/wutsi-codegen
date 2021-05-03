package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.FileSystemUtils
import java.io.File

internal class CorsCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        jdkVersion = "1.8"
    )

    val codegen = CorsCodeGenerator()

    @BeforeEach
    fun setUp() {
        FileSystemUtils.deleteRecursively(File(context.outputDirectory))
    }

    @Test
    fun generate() {
        codegen.generate(OpenAPI(), context)

        val file = File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/servlet/CorsFilter.kt")
        kotlin.test.assertTrue(file.exists())

        val text = file.readText()
        kotlin.test.assertEquals(
            """
                package com.wutsi.test.servlet

                import javax.servlet.Filter
                import javax.servlet.FilterChain
                import javax.servlet.ServletRequest
                import javax.servlet.ServletResponse
                import kotlin.Unit
                import org.springframework.stereotype.Component

                @Component
                public class CorsFilter : Filter {
                  public override fun doFilter(
                    req: ServletRequest,
                    resp: ServletResponse,
                    chain: FilterChain
                  ): Unit {
                    (resp as javax.servlet.http.HttpServletResponse).addHeader(
                        "Access-Control-Allow-Origin",
                        "*"
                    )
                    resp.addHeader(
                        "Access-Control-Allow-Methods",
                        "GET, OPTIONS, HEAD, PUT, POST, DELETE"
                    )
                    resp.addHeader(
                        "Access-Control-Allow-Headers",
                        "Content-Type, Authorization, Content-Length,X-Requested-With,Authorization"
                    )
                    chain.doFilter(req, resp)
                  }
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }
}
