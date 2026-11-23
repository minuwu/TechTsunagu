plugins {
    id("wakatta.android.library")
    id("wakatta.android.compose")
}

android {
    namespace = "com.techtsunagu.wakatta.core.design"
    resourcePrefix = "core_design_"
    compileSdk = 37
    compileSdkMinor = 1
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    api(project(":core:model"))

    implementation(libs.androidx.compose.material.icons.extended)

    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
}
