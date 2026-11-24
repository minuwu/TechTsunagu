plugins {
    id("wakatta.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.techtsunagu.wakatta.core.deeplink"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:navigation"))
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}
