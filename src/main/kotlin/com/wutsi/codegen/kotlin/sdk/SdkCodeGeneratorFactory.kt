package com.wutsi.codegen.kotlin.sdk

import com.wutsi.codegen.Context
import com.wutsi.codegen.core.generator.CodeGeneratorFactory
import com.wutsi.codegen.kotlin.KotlinMapper

class SdkCodeGeneratorFactory : CodeGeneratorFactory {
    override fun create(context: Context) = SdkCodeGenerator(KotlinMapper(context))
}
