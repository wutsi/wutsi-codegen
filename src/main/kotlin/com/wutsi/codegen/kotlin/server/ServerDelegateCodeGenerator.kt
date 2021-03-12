package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.AnnotationSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Endpoint
import com.wutsi.codegen.model.EndpointParameter
import com.wutsi.codegen.model.Request
import com.wutsi.codegen.util.CaseUtil

class ServerDelegateCodeGenerator(mapper: KotlinMapper) : AbstractServerCodeGenerator(mapper) {
    override fun className(endpoint: Endpoint): String =
        CaseUtil.toCamelCase("${endpoint.name}Delegate", true)

    override fun packageName(endpoint: Endpoint, context: Context): String =
        toPackage(context.basePackage, "delegate")

    override fun classAnnotations(endpoint: Endpoint): List<AnnotationSpec> =
        emptyList()

    override fun functionAnnotations(endpoint: Endpoint): List<AnnotationSpec> =
        emptyList()

    override fun requestBodyAnnotations(requestBody: Request?): List<AnnotationSpec> =
        emptyList()

    override fun parameterAnnotations(parameter: EndpointParameter): List<AnnotationSpec> =
        emptyList()
}
