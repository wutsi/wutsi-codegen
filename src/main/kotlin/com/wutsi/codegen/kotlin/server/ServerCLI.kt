package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.generator.AbstractCodeGeneratorCLI
import com.wutsi.codegen.generator.CodeGeneratorFactory
import com.wutsi.codegen.openapi.DefaultOpenAPILoader
import com.wutsi.codegen.openapi.OpenAPILoader

class ServerCLI(
    codeGeneratorFactory: CodeGeneratorFactory = ServerCodeGeneratorFactory(),
    openAPILoader: OpenAPILoader = DefaultOpenAPILoader()
) : AbstractCodeGeneratorCLI(codeGeneratorFactory, openAPILoader) {
    override fun name() = "server"

    override fun description() = "Generate the API Springboot/Kotlin Server code from an OpenAPIV3 specification"
}
