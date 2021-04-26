package com.wutsi.codegen.kotlin.server

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.wutsi.codegen.Context
import com.wutsi.codegen.core.util.CaseUtil
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.model.Endpoint
import com.wutsi.codegen.model.EndpointParameter
import com.wutsi.codegen.model.Request
import org.springframework.stereotype.Service
import java.io.File

class ServerDelegateCodeGenerator(mapper: KotlinMapper) : AbstractServerCodeGenerator(mapper) {
    override fun className(endpoint: Endpoint): String =
        CaseUtil.toCamelCase("${endpoint.name}Delegate", true)

    override fun packageName(endpoint: Endpoint, context: Context): String =
        toPackage(context.basePackage, "delegate")

    override fun classAnnotations(endpoint: Endpoint): List<AnnotationSpec> =
        listOf(
            AnnotationSpec.builder(Service::class).build()
        )

    override fun functionAnnotations(endpoint: Endpoint): List<AnnotationSpec> =
        emptyList()

    override fun requestBodyAnnotations(requestBody: Request?): List<AnnotationSpec> =
        emptyList()

    override fun parameterAnnotations(parameter: EndpointParameter, getter: Boolean): List<AnnotationSpec> =
        emptyList()

    override fun constructorSpec(endpont: Endpoint, context: Context): FunSpec =
        FunSpec.constructorBuilder().build()

    override fun funCodeBloc(endpoint: Endpoint): CodeBlock =
        CodeBlock.builder()
            .addStatement("TODO()")
            .build()

    override fun canGenerate(directory: File, packageName: String, className: String): Boolean {
        val relativePath = "$packageName.$className".replace('.', File.separatorChar)
        return !File(directory.absolutePath + File.separator + relativePath + ".kt").exists()
    }
}
