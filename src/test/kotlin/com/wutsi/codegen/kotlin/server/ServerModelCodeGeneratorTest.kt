package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.kotlin.sdk.SdkCodeGenerator
import com.wutsi.codegen.model.Field
import com.wutsi.codegen.model.Type
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import java.io.File
import java.math.BigDecimal
import kotlin.test.assertEquals

internal class ServerModelCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/sdk",
        basePackage = "com.wutsi.test"
    )

    val codegen = ServerModelCodeGenerator(
        KotlinMapper(context)
    )

    @Test
    fun `toParameterSpec - nullable type with default`() {
        val field = Field(name = "foo", type = String::class, nullable = true, default = "Yo")

        assertEquals("foo: kotlin.String? = \"Yo\"", codegen.toParameterSpec(field).toString())
    }

    @Test
    fun `toParameterSpec - non-nullable type with default`() {
        val field = Field(name = "foo", type = String::class, nullable = false, default = "Yo")

        assertEquals("foo: kotlin.String = \"Yo\"", codegen.toParameterSpec(field).toString())
    }

    @Test
    fun `toParameterSpec - Required Int`() {
        val field = Field(name = "foo", type = Int::class, required = true, nullable = true)
        val spec = codegen.toParameterSpec(field)
        assertEquals("@get:javax.validation.constraints.NotNull foo: kotlin.Int? = null", spec.toString())
    }

    @Test
    fun `toParameterSpec - Required String`() {
        val field = Field(name = "foo", type = String::class, nullable = true, required = true)
        val spec = codegen.toParameterSpec(field)
        assertEquals("@get:javax.validation.constraints.NotBlank foo: kotlin.String? = null", spec.toString())
    }

    @Test
    fun `toParameterSpec - Required List`() {
        val field = Field(name = "foo", type = List::class, nullable = true, required = true)
        val spec = codegen.toParameterSpec(field)
        assertEquals(
            "@get:javax.validation.constraints.NotNull @get:javax.validation.constraints.NotEmpty foo: kotlin.collections.List? = null",
            spec.toString()
        )
    }

    @Test
    fun `toParameterSpec - Min`() {
        val field = Field(name = "foo", type = Int::class, min = BigDecimal(5), nullable = false)
        val spec = codegen.toParameterSpec(field)
        assertEquals(
            "@get:javax.validation.constraints.Min(5) foo: kotlin.Int = 0",
            spec.toString()
        )
    }

    @Test
    fun `toParameterSpec - Max`() {
        val field = Field(name = "foo", type = Int::class, max = BigDecimal(5), nullable = false)
        val spec = codegen.toParameterSpec(field)
        assertEquals(
            "@get:javax.validation.constraints.Max(5) foo: kotlin.Int = 0",
            spec.toString()
        )
    }

    @Test
    fun `toParameterSpec - Size`() {
        val field = Field(name = "foo", type = String::class, minLength = 1, maxLength = 10, nullable = false)
        val spec = codegen.toParameterSpec(field)
        assertEquals(
            "@get:javax.validation.constraints.Size(min=1, max=10) foo: kotlin.String = \"\"",
            spec.toString()
        )
    }

    @Test
    fun `toParameterSpec - Pattern`() {
        val field = Field(name = "foo", type = String::class, pattern = "xxx", nullable = true)
        val spec = codegen.toParameterSpec(field)
        assertEquals(
            "@get:javax.validation.constraints.Pattern(\"xxx\") foo: kotlin.String? = null",
            spec.toString()
        )
    }

    @Test
    fun testToModelTypeSpec() {
        val type = Type(
            name = "Foo",
            packageName = "com.wutsi",
            fields = listOf(
                Field(name = "var1", type = Int::class, required = true, min = BigDecimal(1)),
                Field(name = "var2", type = String::class, nullable = false, required = true)
            )
        )

        val spec = codegen.toModelTypeSpec(type)

        val expected = """
            public data class Foo(
              @get:javax.validation.constraints.NotNull
              @get:javax.validation.constraints.Min(1)
              public val var1: kotlin.Int = 0,
              @get:javax.validation.constraints.NotBlank
              public val var2: kotlin.String = ""
            )
        """.trimIndent()
        kotlin.test.assertEquals(expected, spec.toString().trimIndent())
    }

    @Test
    fun `generate`() {
        val yaml = IOUtils.toString(SdkCodeGenerator::class.java.getResourceAsStream("/api.yaml"), "utf-8")
        codegen.generate(
            openAPI = OpenAPIV3Parser().readContents(yaml).openAPI,
            context = context
        )

        // Model files
        kotlin.test.assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/ErrorResponse.kt").exists())
        kotlin.test.assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/CreateLikeRequest.kt").exists())
        kotlin.test.assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/CreateLikeResponse.kt").exists())
        kotlin.test.assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/GetStatsResponse.kt").exists())
    }
}
