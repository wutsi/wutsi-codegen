package com.wutsi.codegen.core.cli

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.MissingOptionException
import org.apache.commons.cli.Options
import org.apache.commons.cli.UnrecognizedOptionException

abstract class AbstractCLI : CLI {
    companion object {
        const val OPTION_INPUT_FILE = "in"
        const val OPTION_API_NAME = "name"
        const val OPTION_BASE_PACKAGE = "package"
        const val OPTION_OUTPUT_DIR = "out"
        const val OPTION_GITHUB_USER = "github_user"
        const val OPTION_GITHUB_PROJECT = "github_project"
        const val OPTION_JDK_VERSION = "jdk"
        const val OPTION_HELP = "h"
        const val OPTION_SERVICE_CACHE = "service_cache"
        const val OPTION_SERVICE_LOGGER = "service_logger"
        const val OPTION_SERVICE_DATABASE = "service_database"
        const val OPTION_SERVICE_MQUEUE = "service_mqueue"

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
        val fmt = HelpFormatter()
        fmt.width = 160
        fmt.printHelp(
            getCommondLineSyntax(),
            null,
            options,
            footer
        )
    }
}
