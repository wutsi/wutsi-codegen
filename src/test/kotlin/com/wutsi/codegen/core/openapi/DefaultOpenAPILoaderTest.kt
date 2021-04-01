package com.wutsi.codegen.core.openapi

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DefaultOpenAPILoaderTest {

    @Test
    fun load() {
        val spec = OpenAPI()
        val parser = mock<OpenAPIV3Parser>()
        doReturn(spec).whenever(parser).read(any())

        val loader = DefaultOpenAPILoader(parser)

        val url = "http://www.google.ca"
        val result = loader.load(url)

        verify(parser).read(url)
        assertEquals(spec, result)
    }
}
