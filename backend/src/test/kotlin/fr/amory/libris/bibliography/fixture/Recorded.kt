package fr.amory.libris.bibliography.fixture

fun recorded(name: String): String =
    checkNotNull(object {}.javaClass.getResource("/scenarios/$name")) { "no recorded answer $name" }.readText()
