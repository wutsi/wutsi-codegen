package com.wutsi.codegen

import com.wutsi.codegen.kotlin.server.ServerCLI

fun main(args: Array<String>) {
    MainCLI(
        listOf<AbstractCodeGeneratorCLI>(
            ServerCLI()
        )
    ).run(args)
//    val spec = OpenAPIV3Parser().read("https://wutsi-openapi.s3.amazonaws.com/like_api.yaml")
//    System.out.println(spec)
//
//    val context = Context(
//        apiName = "Like",
//        basePackage = "com.wutsi.codegen.test",
//        outputDirectory = "./target/codegen/server"
//    )
//    ServerCodeGenerator(KotlinMapper(context)).generate(spec, context)
//    // SdkCodeGenerator(KotlinMapper(context)).generate(spec, context)
}
