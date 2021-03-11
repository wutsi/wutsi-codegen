package com.wutsi.codegen

import com.wutsi.codegen.sdk.kotlin.KotlinMapper
import com.wutsi.codegen.sdk.kotlin.SdkCodeGenerator
import io.swagger.v3.parser.OpenAPIV3Parser

fun main(args: Array<String>) {
    val spec = OpenAPIV3Parser().read("https://wutsi-openapi.s3.amazonaws.com/like_api.yaml")
    System.out.println(spec)

    val context = Context(
        apiName = "Test",
        basePackage = "com.wutsi.codegen.test",
        outputDirectory = "./target"
    )
    SdkCodeGenerator(KotlinMapper(context)).generate(spec, context)
}
