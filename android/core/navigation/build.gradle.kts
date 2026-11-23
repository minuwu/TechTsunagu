plugins {
    id("wakatta.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.techtsunagu.wakatta.core.navigation"
    resourcePrefix = "core_navigation_"
}

dependencies {
    api(project(":core:model"))
    api(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
