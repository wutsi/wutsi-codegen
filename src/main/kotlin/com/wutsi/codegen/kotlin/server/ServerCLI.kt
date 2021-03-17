package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.AbstractCodeGeneratorCLI
import com.wutsi.codegen.core.generator.CodeGeneratorFactory
import com.wutsi.codegen.core.openapi.DefaultOpenAPILoader
import com.wutsi.codegen.core.openapi.OpenAPILoader
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options

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
    }

    override fun createContext(cmd: CommandLine) = Context(
        apiName = cmd.getOptionValue(OPTION_API_NAME).trim(),
        basePackage = cmd.getOptionValue(OPTION_BASE_PACKAGE).trim(),
        outputDirectory = cmd.getOptionValue(OPTION_OUTPUT_DIR).trim(),
        jdkVersion = cmd.getOptionValue(OPTION_JDK_VERSION)?.trimIndent() ?: DEFAULT_JDK_VERSION,
        githubUser = cmd.getOptionValue(OPTION_GITHUB_USER)?.trim(),
        herokuApp = cmd.getOptionValue(OPTION_HEROKU_APP)?.trim()
    )
}
