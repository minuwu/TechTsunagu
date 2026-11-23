plugins {
    id("wakatta.android.library")
    id("wakatta.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.techtsunagu.wakatta.core.network"
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))

    // Types of the public API (Response, OkHttpClient, JsonObject) are part of what features see.
    api(libs.retrofit)
    api(libs.okhttp)
    api(libs.kotlinx.serialization.json)

    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver3)
    testImplementation(libs.kotlinx.coroutines.test)
}
