package com.wutsi.codegen.core.generator

import com.wutsi.codegen.Context

interface CodeGeneratorFactory {
    fun create(context: Context): CodeGenerator
}
