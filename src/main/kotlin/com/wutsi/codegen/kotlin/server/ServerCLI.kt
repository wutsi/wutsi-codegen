package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.AbstractCodeGeneratorCLI
import com.wutsi.codegen.CodeGeneratorFactory
import com.wutsi.codegen.DefaultOpenAPILoader
import com.wutsi.codegen.OpenAPILoader

class ServerCLI(
    codeGeneratorFactory: CodeGeneratorFactory = ServerCodeGeneratorFactory(),
    openAPILoader: OpenAPILoader = DefaultOpenAPILoader()
) : AbstractCodeGeneratorCLI(codeGeneratorFactory, openAPILoader) {
    override fun name() = "server"

    override fun description() = "Generate the API server code from an OpenAPIV3, using springboot/kotlin"
}
