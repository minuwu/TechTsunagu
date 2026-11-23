plugins {
    id("wakatta.android.feature")
}

android {
    namespace = "com.techtsunagu.wakatta.feature.auth"
    resourcePrefix = "auth_"
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

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver3)
    testImplementation(libs.turbine)
}
