plugins {
    id("wakatta.android.library")
    id("wakatta.android.compose")
    id("wakatta.android.hilt")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    add("implementation", project(":core:model"))
    add("implementation", project(":core:common"))
    add("implementation", project(":core:design"))
    add("implementation", project(":core:navigation"))

    add("implementation", libs.findLibrary("androidx-lifecycle-runtime-compose").get())
    add("implementation", libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
    add("implementation", libs.findLibrary("androidx-hilt-lifecycle-viewmodel-compose").get())
}
