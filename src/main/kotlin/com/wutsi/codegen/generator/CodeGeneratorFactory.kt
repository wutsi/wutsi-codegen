package com.wutsi.codegen.generator

import com.wutsi.codegen.Context

interface CodeGeneratorFactory {
    fun create(context: Context): CodeGenerator
}
