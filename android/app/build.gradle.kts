plugins {
    id("wakatta.android.app")
    id("wakatta.android.compose")
    id("wakatta.android.hilt")
}

// google-services.json is gitignored: a clone without it still builds, and push stays off.
if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")

android {
    namespace = "com.techtsunagu.wakatta"
    compileSdkMinor = 1

    defaultConfig {
        applicationId = "com.techtsunagu.wakatta"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Core modules
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:navigation"))
    implementation(project(":core:deeplink"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:design"))
    implementation(project(":core:database"))
    implementation(project(":core:sync"))
    implementation(project(":core:data"))
    implementation(project(":core:media"))

    // Feature modules
    implementation(project(":feature:auth"))
    implementation(project(":feature:home"))
    implementation(project(":feature:chat"))
    implementation(project(":feature:board"))
    implementation(project(":feature:machines"))
    implementation(project(":feature:nearmiss"))
    implementation(project(":feature:briefing"))
    implementation(project(":feature:notices"))
    implementation(project(":feature:asks"))
    implementation(project(":feature:support"))
    implementation(project(":feature:connect"))
    implementation(project(":feature:insights"))
    implementation(project(":feature:notifications"))
    implementation(project(":feature:update"))
    implementation(project(":feature:course"))

    // AndroidX & Navigation 3 & UI
    implementation(libs.profileinstaller)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.tink.android)

    // Icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Testing
    testImplementation(project(":core:testing"))
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
