package com.wutsi.codegen.kotlin.sdk

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.codegen.AbstractCLI
import com.wutsi.codegen.CodeGenerator
import com.wutsi.codegen.CodeGeneratorFactory
import com.wutsi.codegen.Context
import com.wutsi.codegen.OpenAPILoader
import io.swagger.v3.oas.models.OpenAPI
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SdkCLITest {
    lateinit var codegen: CodeGenerator

    lateinit var factory: CodeGeneratorFactory

    lateinit var openAPILoader: OpenAPILoader

    lateinit var cli: SdkCLI

    @BeforeEach
    fun setUp() {
        codegen = mock()
        factory = mock()
        doReturn(codegen).whenever(factory).create(any())

        openAPILoader = mock()
        cli = SdkCLI(factory, openAPILoader)
    }

    @Test
    fun `run`() {
        val url = "https://localhost:8080/like.yaml"
        val openAPI = OpenAPI()
        doReturn(openAPI).whenever(openAPILoader).load(url)

        val args = arrayOf(
            "server",
            "-a like",
            "-p com.wutsi.test",
            "-i $url",
            "-o ./target",
            "-j 11",
            "-g foo"
        )
        cli.run(args)

        val spec = argumentCaptor<OpenAPI>()
        val context = argumentCaptor<Context>()
        verify(codegen).generate(spec.capture(), context.capture())

        kotlin.test.assertEquals(openAPI, spec.firstValue)

        kotlin.test.assertEquals("like", context.firstValue.apiName)
        kotlin.test.assertEquals("./target", context.firstValue.outputDirectory)
        kotlin.test.assertEquals("com.wutsi.test", context.firstValue.basePackage)
        kotlin.test.assertEquals("11", context.firstValue.jdkVersion)
        kotlin.test.assertEquals("foo", context.firstValue.githubUser)
    }

    @Test
    fun `run with required parameters`() {
        val url = "https://localhost:8080/like.yaml"
        val openAPI = OpenAPI()
        doReturn(openAPI).whenever(openAPILoader).load(url)

        val args = arrayOf(
            "server",
            "-a like",
            "-p com.wutsi.test",
            "-i $url",
            "-o ./target"
        )
        cli.run(args)

        val context = argumentCaptor<Context>()
        verify(codegen).generate(any(), context.capture())
        kotlin.test.assertEquals("like", context.firstValue.apiName)
        kotlin.test.assertEquals("./target", context.firstValue.outputDirectory)
        kotlin.test.assertEquals("com.wutsi.test", context.firstValue.basePackage)
        kotlin.test.assertEquals(AbstractCLI.DEFAULT_JDK_VERSION, context.firstValue.jdkVersion)
        kotlin.test.assertNull(context.firstValue.githubUser)
    }

    @Test
    fun `run - missing api-name`() {
        val url = "https://localhost:8080/like.yaml"

        val args = arrayOf(
            "server",
            "-p com.wutsi.test",
            "-i $url",
            "-o ./target"
        )
        cli.run(args)

        verify(codegen, never()).generate(any(), any())
    }

    @Test
    fun `run - missing base-package`() {
        val url = "https://localhost:8080/like.yaml"

        val args = arrayOf(
            "server",
            "-a test",
            "-p com.wutsi.test",
            "-i $url"
        )
        cli.run(args)

        verify(codegen, never()).generate(any(), any())
    }

    @Test
    fun `run - missing input-file`() {
        val url = "https://localhost:8080/like.yaml"

        val args = arrayOf(
            "server",
            "-a test",
            "-p com.wutsi.test",
            "-o ./target"
        )
        cli.run(args)

        verify(codegen, never()).generate(any(), any())
    }

    @Test
    fun `run - missing output-dir`() {
        val url = "https://localhost:8080/like.yaml"

        val args = arrayOf(
            "server",
            "-a test",
            "-p com.wutsi.test",
            "-i $url",
        )
        cli.run(args)

        verify(codegen, never()).generate(any(), any())
    }
}
