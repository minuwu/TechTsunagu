plugins {
    id("wakatta.android.feature")
}

android {
    namespace = "com.techtsunagu.wakatta.feature.insights"
    resourcePrefix = "insights_"
    compileSdkMinor = 1
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(libs.androidx.compose.material.icons.extended)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(project(":core:testing"))
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
