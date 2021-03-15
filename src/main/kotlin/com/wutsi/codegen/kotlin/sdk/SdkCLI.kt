package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.generator.AbstractCodeGeneratorCLI
import com.wutsi.codegen.generator.CodeGeneratorFactory
import com.wutsi.codegen.kotlin.server.ServerCodeGeneratorFactory
import com.wutsi.codegen.openapi.DefaultOpenAPILoader
import com.wutsi.codegen.openapi.OpenAPILoader

class SdkCLI(
    codeGeneratorFactory: CodeGeneratorFactory = ServerCodeGeneratorFactory(),
    openAPILoader: OpenAPILoader = DefaultOpenAPILoader()
) : AbstractCodeGeneratorCLI(codeGeneratorFactory, openAPILoader) {
    override fun name() = "sdk"

    override fun description() = "Generate the API Kotlin SDK from an OpenAPIV3 specification"
}
