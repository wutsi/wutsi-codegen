package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.AbstractCodeGeneratorCLI
import com.wutsi.codegen.CodeGeneratorFactory
import com.wutsi.codegen.DefaultOpenAPILoader
import com.wutsi.codegen.OpenAPILoader
import com.wutsi.codegen.kotlin.server.ServerCodeGeneratorFactory

class SdkCLI(
    codeGeneratorFactory: CodeGeneratorFactory = ServerCodeGeneratorFactory(),
    openAPILoader: OpenAPILoader = DefaultOpenAPILoader()
) : AbstractCodeGeneratorCLI(codeGeneratorFactory, openAPILoader) {
    override fun name() = "sdk"

    override fun description() = "Generate the API Kotlin SDK from an OpenAPIV3 specification"
}
