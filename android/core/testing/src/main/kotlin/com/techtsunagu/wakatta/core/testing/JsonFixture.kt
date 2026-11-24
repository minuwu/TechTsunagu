package com.techtsunagu.wakatta.core.testing

object JsonFixture {
    fun load(name: String): String {
        val fileName = if (name.endsWith(".json")) name else "$name.json"
        val path = "fixtures/$fileName"
        val stream = javaClass.classLoader?.getResourceAsStream(path)
            ?: Thread.currentThread().contextClassLoader?.getResourceAsStream(path)
            ?: throw IllegalArgumentException("Fixture not found at path: $path")
        return stream.bufferedReader().use { it.readText() }
    }
}
