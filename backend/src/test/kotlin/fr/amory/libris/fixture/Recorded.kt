package fr.amory.libris.fixture

fun recorded(name: String): String =
    checkNotNull(object {}.javaClass.getResource("/scenarios/$name")) { "no recorded answer $name" }.readText()
