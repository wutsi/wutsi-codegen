package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractCodeGeneratorCLI
import com.wutsi.codegen.core.generator.CodeGeneratorFactory
import com.wutsi.codegen.core.openapi.DefaultOpenAPILoader
import com.wutsi.codegen.core.openapi.OpenAPILoader
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import java.net.URL

class ServerCLI(
    codeGeneratorFactory: CodeGeneratorFactory = ServerCodeGeneratorFactory(),
    openAPILoader: OpenAPILoader = DefaultOpenAPILoader()
) : AbstractCodeGeneratorCLI(codeGeneratorFactory, openAPILoader) {
    companion object {
        const val OPTION_HEROKU_APP = "heroku"
    }

    override fun name() = "server"

    override fun description() = "Generate the API Springboot/Kotlin Server code from an OpenAPIV3 specification"

    override fun addOptions(options: Options) {
        super.addOptions(options)

        options.addOption(
            Option.builder(OPTION_HEROKU_APP)
                .hasArg()
                .argName("heroku-app")
                .desc(
                    "Heroku application name. This will trigger the deployment when merging to `master` branch.\n" +
                        "                                IMPORTANT: The github secret HEROKU_API_KEY must be configured."
                )
                .build()
        )
        options.addOption(
            Option.builder(OPTION_SERVICE_CACHE)
                .hasArg(false)
                .desc("Attach a cache to the API")
                .build()
        )
        options.addOption(
            Option.builder(OPTION_SERVICE_DATABASE)
                .hasArg(false)
                .desc("Attach a database to the API")
                .build()
        )
        options.addOption(
            Option.builder(OPTION_SERVICE_LOGGER)
                .hasArg(false)
                .desc("Attach a logger to the API")
                .build()
        )
        options.addOption(
            Option.builder(OPTION_SERVICE_MQUEUE)
                .hasArg(false)
                .desc("Attach a queue to the API")
                .build()
        )
    }

    override fun createContext(cmd: CommandLine): Context {
        val context = Context(
            apiName = cmd.getOptionValue(OPTION_API_NAME).trim(),
            basePackage = cmd.getOptionValue(OPTION_BASE_PACKAGE).trim(),
            outputDirectory = cmd.getOptionValue(OPTION_OUTPUT_DIR).trim(),
            jdkVersion = cmd.getOptionValue(OPTION_JDK_VERSION)?.trimIndent() ?: DEFAULT_JDK_VERSION,
            githubUser = cmd.getOptionValue(OPTION_GITHUB_USER)?.trim(),
            githubProject = cmd.getOptionValue(OPTION_GITHUB_PROJECT)?.trim(),
            herokuApp = cmd.getOptionValue(OPTION_HEROKU_APP)?.trim(),
            inputUrl = URL(cmd.getOptionValue(OPTION_INPUT_FILE).trim())
        )
        if (cmd.hasOption(OPTION_SERVICE_CACHE))
            context.addService(Context.SERVICE_CACHE)
        if (cmd.hasOption(OPTION_SERVICE_DATABASE))
            context.addService(Context.SERVICE_DATABASE)
        if (cmd.hasOption(OPTION_SERVICE_MQUEUE))
            context.addService(Context.SERVICE_MQUEUE)
        if (cmd.hasOption(OPTION_SERVICE_LOGGER))
            context.addService(Context.SERVICE_LOGGING)

        return context
    }
}
