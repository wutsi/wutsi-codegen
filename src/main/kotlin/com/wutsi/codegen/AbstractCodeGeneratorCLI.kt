package com.wutsi.codegen

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options

abstract class AbstractCodeGeneratorCLI(
    private val codeGeneratorFactory: CodeGeneratorFactory,
    private val openAPILoader: OpenAPILoader
) : AbstractCLI() {
    abstract fun name(): String

    abstract fun description(): String

    override fun getCommondLineSyntax() = "java wutsi-codegen-<version>.jar ${name()} [options]"

    override fun addOptions(options: Options) {
        options.addOption(
            Option.builder(OPTION_INPUT_FILE)
                .hasArg()
                .argName("openapi-file-url")
                .desc("(REQUIRED) URL of the OpenAPIV3 file that describe the API")
                .required()
                .build()
        )
        options.addOption(
            Option.builder(OPTION_API_NAME)
                .hasArg()
                .argName("api-name")
                .desc("(REQUIRED) Name of the API. Ex: like")
                .required()
                .build()
        )
        options.addOption(
            Option.builder(OPTION_BASE_PACKAGE)
                .hasArg()
                .argName("base-package")
                .desc("(REQUIRED) Base package of the api. Ex: com.foo.bar")
                .required()
                .build()
        )
        options.addOption(
            Option.builder(OPTION_OUTPUT_DIR)
                .hasArg()
                .argName("output-dir")
                .desc("(REQUIRED) Output directory. Where to store the generated files")
                .required()
                .build()
        )
        options.addOption(
            Option.builder(OPTION_JDK_VERSION)
                .hasArg()
                .argName("jdk-version")
                .desc("Version of the JDK of the project. Default: $DEFAULT_JDK_VERSION")
                .build()
        )
        options.addOption(
            Option.builder(OPTION_GITHUB_USER)
                .hasArg()
                .argName("github-user")
                .desc("Your github username")
                .build()
        )
    }

    override fun run(args: Array<String>, cmd: CommandLine) {
        if (cmd.hasOption(OPTION_HELP)) {
            printHelp()
        } else {
            val url = cmd.getOptionValue(OPTION_INPUT_FILE).trim()
            val spec = openAPILoader.load(url)
            val context = createContext(cmd)
            codeGeneratorFactory.create(context).generate(spec, context)
        }
    }

    private fun createContext(cmd: CommandLine) = Context(
        apiName = cmd.getOptionValue(OPTION_API_NAME).trim(),
        basePackage = cmd.getOptionValue(OPTION_BASE_PACKAGE).trimIndent(),
        outputDirectory = cmd.getOptionValue(OPTION_OUTPUT_DIR).trim(),
        jdkVersion = cmd.getOptionValue(OPTION_JDK_VERSION)?.trimIndent() ?: DEFAULT_JDK_VERSION,
        githubUser = cmd.getOptionValue(OPTION_GITHUB_USER)?.trim()
    )
}
