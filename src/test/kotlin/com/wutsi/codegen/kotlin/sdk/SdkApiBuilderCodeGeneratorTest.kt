package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

internal class SdkApiBuilderCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/sdk",
        basePackage = "com.wutsi.test"
    )

    val codegen = SdkApiBuilderCodeGenerator(KotlinMapper(context))

    @Test
    fun `generate`() {
        codegen.generate(
            openAPI = OpenAPI(),
            context = context
        )

        val file = File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/TestApiBuilder.kt")
        assertTrue(file.exists())

        val text = file.readText()
        kotlin.test.assertEquals(
            """
                package com.wutsi.test

                import com.fasterxml.jackson.databind.ObjectMapper

                public class TestApiBuilder {
                  public fun build(env: Environment, mapper: ObjectMapper) = Feign.builder()
                    .client(feign.okhttp.OkHttpClient())
                    .encoder(feign.jackson.JacksonEncoder(mapper))
                    .decoder(feign.jackson.JacksonDecoder(mapper))
                    .logger(feign.slf4j.Slf4jLogger())
                    .target(TestApi::class.java, env.url)
                }
            """.trimIndent(),
            text.trimIndent()
        )
    }
}
