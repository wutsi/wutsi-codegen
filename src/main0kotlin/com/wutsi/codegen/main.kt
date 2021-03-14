package com.wutsi.codegen

import com.wutsi.codegen.kotlin.sdk.SdkCLI
import com.wutsi.codegen.kotlin.server.ServerCLI

fun main(args: Array<String>) {
    MainCLI(
        listOf(
            ServerCLI(),
            SdkCLI()
        )
    ).run(args)
}
