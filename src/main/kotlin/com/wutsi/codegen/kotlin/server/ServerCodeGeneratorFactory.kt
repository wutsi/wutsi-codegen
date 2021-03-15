package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.CodeGeneratorFactory
import com.wutsi.codegen.kotlin.KotlinMapper

class ServerCodeGeneratorFactory : CodeGeneratorFactory {
    override fun create(context: Context) = ServerCodeGenerator(KotlinMapper(context))
}
