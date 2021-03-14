package com.wutsi.codegen

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.MissingOptionException
import org.apache.commons.cli.Options
import org.apache.commons.cli.UnrecognizedOptionException

abstract class AbstractCLI : CLI {
    companion object {
        const val OPTION_INPUT_FILE = "i"
        const val OPTION_API_NAME = "a"
        const val OPTION_BASE_PACKAGE = "p"
        const val OPTION_OUTPUT_DIR = "o"
        const val OPTION_GITHUB_USER = "g"
        const val OPTION_JDK_VERSION = "j"
        const val OPTION_HELP = "h"

        const val DEFAULT_JDK_VERSION = "1.8"
    }

    protected abstract fun addOptions(options: Options)

    protected abstract fun getCommondLineSyntax(): String

    protected abstract fun run(args: Array<String>, cmd: CommandLine)

    override fun run(args: Array<String>) {
        val options = Options()
        addOptions(options)

        try {
            val cmd = DefaultParser().parse(options, args)
            run(args, cmd)
        } catch (ex: MissingOptionException) {
            ex.printStackTrace()
            printHelp()
        } catch (ex: UnrecognizedOptionException) {
            ex.printStackTrace()
            printHelp()
        }
    }

    protected fun printHelp(footer: String? = null) {
        val options = Options()
        addOptions(options)

        System.out.println()
        HelpFormatter().printHelp(
            getCommondLineSyntax(),
            null,
            options,
            footer
        )
    }
}
