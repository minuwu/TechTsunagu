plugins {
    id("wakatta.android.feature")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.techtsunagu.wakatta.feature.notifications"
    resourcePrefix = "notifications_"
    compileSdkMinor = 1
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(project(":core:network"))
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)

    // Firebase is optional at build time: nothing here needs google-services.json (only :app applies that plugin).
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(project(":core:testing"))
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver3)
}
