import com.android.build.api.dsl.CommonExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

extensions.configure<CommonExtension> {
    buildFeatures.compose = true
}

configure<ComposeCompilerGradlePluginExtension> {
    if (project.findProperty("wakatta.composeReports") == "true") {
        val reportsDir = layout.buildDirectory.dir("compose-reports")
        metricsDestination.set(reportsDir.map { it.dir("metrics") })
        reportsDestination.set(reportsDir.map { it.dir("reports") })
    }
}

dependencies {
    val bom = libs.findLibrary("androidx-compose-bom").get()
    add("implementation", platform(bom))
    add("androidTestImplementation", platform(bom))

    add("implementation", libs.findLibrary("androidx-compose-ui").get())
    add("implementation", libs.findLibrary("androidx-compose-ui-graphics").get())
    add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
    add("implementation", libs.findLibrary("androidx-compose-material3").get())
    add("implementation", libs.findLibrary("androidx-activity-compose").get())

    add("debugImplementation", libs.findLibrary("androidx-compose-ui-tooling").get())
    add("debugImplementation", libs.findLibrary("androidx-compose-ui-test-manifest").get())
}
