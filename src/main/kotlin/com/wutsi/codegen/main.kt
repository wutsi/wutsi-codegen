package com.wutsi.codegen

import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.kotlin.sdk.SdkCodeGenerator
import io.swagger.v3.parser.OpenAPIV3Parser

fun main(args: Array<String>) {
    val spec = OpenAPIV3Parser().read("https://wutsi-openapi.s3.amazonaws.com/like_api.yaml")
    System.out.println(spec)

    val context = Context(
        apiName = "Like",
        basePackage = "com.wutsi.codegen.test",
        outputDirectory = "./target/codegen/sdk"
    )
    //ServerCodeGenerator(KotlinMapper(context)).generate(spec, context)
    SdkCodeGenerator(KotlinMapper(context)).generate(spec, context)
}
