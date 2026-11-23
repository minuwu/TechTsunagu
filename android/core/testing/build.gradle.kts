plugins {
    id("wakatta.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.techtsunagu.wakatta.core.testing"
}

dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.okhttp)
    api(libs.mockwebserver3)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
}
