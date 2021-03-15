package com.wutsi.codegen

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.wutsi.codegen.core.generator.AbstractCodeGeneratorCLI
import org.junit.jupiter.api.Test

internal class MainCLITest {
    @Test
    fun `run a code generator`() {
        val foo = createCodeGeneratorCLI("foo")
        val bar = createCodeGeneratorCLI("bar")

        val cli = MainCLI(listOf(foo, bar))

        var args = arrayOf("bar", "-i https://xxx.com", "-o ./target")
        cli.run(args)

        verify(foo, never()).run(args)
        verify(bar).run(args)
    }

    @Test
    fun `run invalid command`() {
        val foo = createCodeGeneratorCLI("foo")
        val bar = createCodeGeneratorCLI("bar")

        val cli = MainCLI(listOf(foo, bar))

        var args = arrayOf("xx", "xx")
        cli.run(args)

        verify(foo, never()).run(args)
        verify(bar, never()).run(args)
    }

    fun createCodeGeneratorCLI(name: String): AbstractCodeGeneratorCLI {
        val cli = mock<AbstractCodeGeneratorCLI>()
        doReturn(name).whenever(cli).name()
        doReturn(name).whenever(cli).description()
        return cli
    }
}
