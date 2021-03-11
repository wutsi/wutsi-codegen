package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Field
import com.wutsi.codegen.model.Type
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import java.io.File
import java.math.BigDecimal
import java.time.LocalDate
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SdkModelCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = System.getProperty("user.home") + "/wutsi/codegen/sdk",
        basePackage = "com.wutsi.test"
    )

    val codegen = SdkModelCodeGenerator(
        KotlinMapper(context)
    )

    @Test
    fun `defaultValue - nullable field with no default`() {
        assertEquals("null", codegen.defaultValue(Field(name = "foo", type = String::class, nullable = true, default = null)))

        assertEquals("\"\"", codegen.defaultValue(Field(name = "foo", type = String::class, nullable = false)))
        assertEquals("0", codegen.defaultValue(Field(name = "foo", type = Int::class, nullable = false)))
        assertEquals("0", codegen.defaultValue(Field(name = "foo", type = Long::class, nullable = false)))
        assertEquals("0", codegen.defaultValue(Field(name = "foo", type = Float::class, nullable = false)))
        assertEquals("0", codegen.defaultValue(Field(name = "foo", type = Double::class, nullable = false)))
        assertEquals("LocalDate.now()", codegen.defaultValue(Field(name = "foo", type = LocalDate::class, nullable = false)))
        assertEquals("OffsetDateTime.now()", codegen.defaultValue(Field(name = "foo", type = OffsetDateTime::class, nullable = false)))
        assertEquals("emptyList()", codegen.defaultValue(Field(name = "foo", type = List::class, nullable = false)))
    }

    @Test
    fun `defaultValue - non-nullable field with default`() {
        assertEquals("\"Yo\"", codegen.defaultValue(Field(name = "foo", type = String::class, nullable = false, default = "Yo")))
        assertEquals("1", codegen.defaultValue(Field(name = "foo", type = Int::class, nullable = false, default = "1")))
        assertEquals("2", codegen.defaultValue(Field(name = "foo", type = Long::class, nullable = false, default = "2")))
        assertEquals("3.0", codegen.defaultValue(Field(name = "foo", type = Float::class, nullable = false, default = "3.0")))
        assertEquals("4.0", codegen.defaultValue(Field(name = "foo", type = Double::class, nullable = false, default = "4.0")))
    }

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
        val field = Field(name = "foo", type = Int::class, required = true)
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
        val field = Field(name = "foo", type = String::class, pattern = "xxx")
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
                Field(name = "var1", type = Int::class),
                Field(name = "var2", type = String::class)
            )
        )

        val spec = codegen.toModelTypeSpec(type)

        val expected = """
            public data class Foo(
              public val var1: kotlin.Int? = null,
              public val var2: kotlin.String? = null
            )
        """.trimIndent()
        assertEquals(expected, spec.toString().trimIndent())
    }

    @Test
    fun `generate`() {
        val yaml = IOUtils.toString(SdkCodeGenerator::class.java.getResourceAsStream("/api.yaml"))
        codegen.generate(
            openAPI = OpenAPIV3Parser().readContents(yaml).openAPI,
            context = context
        )

        // Model files
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/ErrorResponse.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/CreateLikeRequest.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/CreateLikeResponse.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/GetStatsResponse.kt").exists())
    }
}
