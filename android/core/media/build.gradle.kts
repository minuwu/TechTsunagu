plugins {
    id("wakatta.android.library")
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

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
