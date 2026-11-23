plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation("com.android.tools.build:gradle:${libs.findVersion("agp").get().requiredVersion}")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.findVersion("kotlin").get().requiredVersion}")
    implementation("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:${libs.findVersion("kotlin").get().requiredVersion}")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:${libs.findVersion("ksp").get().requiredVersion}")
    implementation("com.google.dagger:hilt-android-gradle-plugin:${libs.findVersion("hilt").get().requiredVersion}")
    implementation("androidx.room:room-gradle-plugin:${libs.findVersion("room").get().requiredVersion}")
}
