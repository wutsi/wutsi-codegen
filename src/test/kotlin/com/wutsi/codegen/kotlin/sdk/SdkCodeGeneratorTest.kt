package com.wutsi.codegen.kotlin.sdk

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.CodeGenerator
import com.wutsi.codegen.github.GitCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class SdkCodeGeneratorTest {
    val context = mock<Context>()

    @Test
    fun `run all generators`() {
        val gen1 = mock<CodeGenerator>()
        val gen2 = mock<CodeGenerator>()
        val gen3 = mock<CodeGenerator>()
        val codegen = SdkCodeGenerator(
            mapper = KotlinMapper(context),
            generators = listOf(gen1, gen2, gen3)
        )

        val openAPI = OpenAPI()
        codegen.generate(openAPI, context)

        verify(gen1).generate(openAPI, context)
        verify(gen2).generate(openAPI, context)
        verify(gen3).generate(openAPI, context)
    }

    @Test
    fun `generators`() {
        val codegen = SdkCodeGenerator(mapper = KotlinMapper(context))

        assertEquals(8, codegen.generators.size)
        assertTrue(codegen.generators[0] is SdkDtoCodeGenerator)
        assertTrue(codegen.generators[1] is SdkApiCodeGenerator)
        assertTrue(codegen.generators[2] is SdkMavenCodeGenerator)
        assertTrue(codegen.generators[3] is GitCodeGenerator)
        assertTrue(codegen.generators[4] is SdkGithubActionsCodeGenerator)
        assertTrue(codegen.generators[5] is SdkReadmeCodeGenerator)
        assertTrue(codegen.generators[6] is SdkEnvironmentGenerator)
        assertTrue(codegen.generators[7] is SdkApiBuilderCodeGenerator)
    }
}
