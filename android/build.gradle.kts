import groovy.json.JsonSlurper

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.services) apply false
}

tasks.register("checkModuleGraph") {
    group = "verification"
    description = "Enforces architectural module dependency rules defined in module-rules.json"

    val rulesFile = file("module-rules.json")
    inputs.file(rulesFile)

    doLast {
        if (!rulesFile.exists()) {
            throw GradleException("module-rules.json not found at ${rulesFile.absolutePath}")
        }
        val parsedJson = JsonSlurper().parseText(rulesFile.readText()) as? Map<*, *>
            ?: throw GradleException("Invalid JSON in module-rules.json")

        @Suppress("UNCHECKED_CAST")
        val rules = (parsedJson["rules"] as? Map<String, List<String>>) ?: emptyMap()
        @Suppress("UNCHECKED_CAST")
        val forbidden = (parsedJson["forbidden"] as? Map<String, List<String>>) ?: emptyMap()

        val violations = mutableListOf<String>()

        fun matchesPattern(modulePath: String, pattern: String): Boolean {
            return if (pattern.endsWith("*")) {
                modulePath.startsWith(pattern.removeSuffix("*"))
            } else {
                modulePath == pattern
            }
        }

        for (proj in project.subprojects) {
            val projPath = proj.path

            val allowedPatterns: List<String> = (rules[projPath]
                ?: if (projPath.startsWith(":feature:")) rules[":feature:*"] else null)
                ?: emptyList()

            val forbiddenPatterns: List<String> = (forbidden[projPath]
                ?: if (projPath.startsWith(":feature:")) forbidden[":feature:*"] else null)
                ?: emptyList()

            val directProjectDeps = mutableSetOf<String>()
            for (config in proj.configurations) {
                for (dep in config.dependencies.withType<ProjectDependency>()) {
                    if (dep.path != projPath) {
                        directProjectDeps.add(dep.path)
                    }
                }
            }

            for (depPath in directProjectDeps) {
                for (pattern in forbiddenPatterns) {
                    if (matchesPattern(depPath, pattern)) {
                        violations.add("Forbidden dependency: $projPath -> $depPath (violates rule '$pattern')")
                    }
                }

                if (!allowedPatterns.contains("*")) {
                    val isAllowed = allowedPatterns.any { pattern -> matchesPattern(depPath, pattern) }
                    if (!isAllowed) {
                        violations.add("Disallowed dependency: $projPath -> $depPath (not in allowed list: $allowedPatterns)")
                    }
                }
            }
        }

        if (violations.isNotEmpty()) {
            violations.forEach { logger.error("MODULE GRAPH VIOLATION: $it") }
            throw GradleException("checkModuleGraph failed with ${violations.size} violation(s):\n" + violations.joinToString("\n"))
        } else {
            println("SUCCESS: checkModuleGraph verified all module dependencies against module-rules.json")
        }
    }
}

val checkTask = if (tasks.names.contains("check")) {
    tasks.named("check")
} else {
    tasks.register("check") {
        group = "verification"
    }
}
checkTask.configure {
    dependsOn("checkModuleGraph")
}
