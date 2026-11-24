import androidx.room.gradle.RoomExtension

plugins {
    id("com.google.devtools.ksp")
    id("androidx.room")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

configure<RoomExtension> {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("implementation", libs.findLibrary("room-runtime").get())
    add("implementation", libs.findLibrary("room-ktx").get())
    add("ksp", libs.findLibrary("room-compiler").get())
}
