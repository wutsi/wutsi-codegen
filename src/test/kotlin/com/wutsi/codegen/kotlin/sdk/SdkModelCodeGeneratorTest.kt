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
        outputDirectory = "./target/wutsi/codegen/sdk",
        basePackage = "com.wutsi.test"
    )

    val codegen = SdkModelCodeGenerator(
        KotlinMapper(context)
    )

    @Test
    fun `defaultValue - nullable field with no default`() {
        assertEquals("null", codegen.defaultValue(Field(name = "foo", type = String::class, nullable = true, default = null)))

        assertEquals("\"\"", codegen.defaultValue(Field(name = "foo", type = String::class, nullable = false), true))
        assertEquals("0", codegen.defaultValue(Field(name = "foo", type = Int::class, nullable = false), true))
        assertEquals("0", codegen.defaultValue(Field(name = "foo", type = Long::class, nullable = false), true))
        assertEquals("0", codegen.defaultValue(Field(name = "foo", type = Float::class, nullable = false), true))
        assertEquals("0", codegen.defaultValue(Field(name = "foo", type = Double::class, nullable = false), true))
        assertEquals("LocalDate.now()", codegen.defaultValue(Field(name = "foo", type = LocalDate::class, nullable = false), true))
        assertEquals("OffsetDateTime.now()", codegen.defaultValue(Field(name = "foo", type = OffsetDateTime::class, nullable = false), true))
        assertEquals("emptyList()", codegen.defaultValue(Field(name = "foo", type = List::class, nullable = false), true))
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
              public val var1: kotlin.Int = 0,
              public val var2: kotlin.String = ""
            )
        """.trimIndent()
        assertEquals(expected, spec.toString().trimIndent())
    }

    @Test
    fun `generate`() {
        val yaml = IOUtils.toString(SdkCodeGenerator::class.java.getResourceAsStream("/api.yaml"), "utf-8")
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
