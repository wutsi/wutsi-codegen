package com.wutsi.codegen

interface CodeGeneratorFactory {
    fun create(context: Context): CodeGenerator
}
