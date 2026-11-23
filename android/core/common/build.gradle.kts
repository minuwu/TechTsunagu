plugins {
    id("wakatta.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.techtsunagu.wakatta.core.common"
}

dependencies {
    api(project(":core:model"))
    api(libs.javax.inject)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
}
