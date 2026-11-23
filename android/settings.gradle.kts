pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "wakatta"

include(":app")

include(
    ":core:model",
    ":core:common",
    ":core:navigation",
    ":core:deeplink",
    ":core:network",
    ":core:datastore",
    ":core:design",
    ":core:database",
    ":core:sync",
    ":core:data",
    ":core:media",
    ":core:testing",
)

include(
    ":feature:auth",
    ":feature:home",
    ":feature:chat",
    ":feature:board",
    ":feature:machines",
    ":feature:nearmiss",
    ":feature:briefing",
    ":feature:notices",
    ":feature:asks",
    ":feature:support",
    ":feature:connect",
    ":feature:insights",
    ":feature:notifications",
    ":feature:update",
    ":feature:course",
)
