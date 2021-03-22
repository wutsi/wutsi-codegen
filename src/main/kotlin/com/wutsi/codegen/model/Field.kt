package com.wutsi.codegen.model

import java.math.BigDecimal
import kotlin.reflect.KClass

data class Field(
    val name: String,
    val type: KClass<*>,
    val default: String? = null,
    val required: Boolean = false,
    val min: BigDecimal? = null,
    val max: BigDecimal? = null,
    val pattern: String? = null,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val minItems: Int? = null,
    val maxItems: Int? = null,
    val nullable: Boolean = false
)
