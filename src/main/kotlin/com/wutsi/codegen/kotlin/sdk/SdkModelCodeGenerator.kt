package com.wutsi.codegen.kotlin.sdk

import com.squareup.kotlinpoet.AnnotationSpec
import com.wutsi.codegen.kotlin.AbstractModelCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Field

class SdkModelCodeGenerator(mapper: KotlinMapper) : AbstractModelCodeGenerator(mapper) {
    override fun parameterAnnotationSpecs(field: Field): List<AnnotationSpec> = emptyList()
}
