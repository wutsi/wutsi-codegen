package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.AnnotationSpec
import com.wutsi.codegen.kotlin.AbstractDtoCodeGenerator
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Field

class ServerDtoCodeGenerator(mapper: KotlinMapper) : AbstractDtoCodeGenerator(mapper) {
    override fun parameterAnnotationSpecs(field: Field): List<AnnotationSpec> =
        toValidationAnnotationSpecs(field)
}
