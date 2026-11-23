plugins {
    id("wakatta.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.techtsunagu.wakatta.core.data"
}

dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    api(libs.javax.inject)
    implementation(project(":core:network"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver3)
}
