package com.wutsi.codegen.kotlin.sdk

import com.squareup.kotlinpoet.AnnotationSpec
import com.wutsi.codegen.kotlin.AbstractDtoCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Field

class SdkDtoCodeGenerator(mapper: KotlinMapper) : AbstractDtoCodeGenerator(mapper) {
    override fun parameterAnnotationSpecs(field: Field, getter: Boolean): List<AnnotationSpec> = emptyList()
}
