package com.wutsi.codegen.kotlin

import com.wutsi.codegen.CodeGenerator
import com.wutsi.codegen.Context
import java.io.File

abstract class AbstractKotlinCodeGenerator : CodeGenerator {
    protected fun getSourceDirectory(context: Context): File =
        File(context.outputDirectory + "${File.separator}src${File.separator}main${File.separator}kotlin")
}
