plugins {
    id("wakatta.android.library")
    id("wakatta.android.compose")
    id("wakatta.android.hilt")
}

android {
    namespace = "com.techtsunagu.wakatta.core.media"
}

dependencies {
    api(project(":core:model"))
    api(project(":core:common"))
    api(libs.javax.inject)

    implementation(libs.zxing.core)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.mlkit.barcode)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
