package com.wutsi.codegen.kotlin.server

import com.wutsi.codegen.Context
import com.wutsi.codegen.kotlin.KotlinMapper
import com.wutsi.codegen.kotlin.sdk.SdkCodeGenerator
import io.swagger.v3.parser.OpenAPIV3Parser
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertTrue

internal class ServerCodeGeneratorTest {
    val context = Context(
        apiName = "Test",
        outputDirectory = "./target/wutsi/codegen/server",
        basePackage = "com.wutsi.test",
        githubUser = "foo",
        herokuApp = null
    )

    val codegen = ServerCodeGenerator(
        KotlinMapper(context)
    )

    @Test
    fun `generate`() {
        val yaml = IOUtils.toString(SdkCodeGenerator::class.java.getResourceAsStream("/api.yaml"), "utf-8")
        codegen.generate(
            openAPI = OpenAPIV3Parser().readContents(yaml).openAPI,
            context = context
        )

        // Controller
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/endpoint/CreateController.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/endpoint/DeleteController.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/endpoint/StatsController.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/endpoint/SearchController.kt").exists())

        // Delegate
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/CreateDelegate.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/DeleteDelegate.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/StatsDelegate.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/delegate/SearchDelegate.kt").exists())

        // Model files
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/ErrorResponse.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/CreateLikeRequest.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/CreateLikeResponse.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/GetStatsResponse.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/GetStatsResponse.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/SearchLikeResponse.kt").exists())
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/model/Like.kt").exists())

        // Launcher
        assertTrue(File("${context.outputDirectory}/src/main/kotlin/com/wutsi/test/Application.kt").exists())

        // Maven
        assertTrue(File("${context.outputDirectory}/pom.xml").exists())
        assertTrue(File("${context.outputDirectory}/settings.xml").exists())

        // .editorconfig
        assertTrue(File("${context.outputDirectory}/.editorconfig").exists())

        // Github Workflows
        assertTrue(File("${context.outputDirectory}/.github/workflows/master.yml").exists())
        assertTrue(File("${context.outputDirectory}/.github/workflows/pull_request.yml").exists())

        // .gitignore
        assertTrue(File("${context.outputDirectory}/.gitignore").exists())

        // Config
        assertTrue(File("${context.outputDirectory}/src/main/resources/application.yml").exists())
        assertTrue(File("${context.outputDirectory}/src/main/resources/application-test.yml").exists())
        assertTrue(File("${context.outputDirectory}/src/main/resources/application-prod.yml").exists())
    }
}
