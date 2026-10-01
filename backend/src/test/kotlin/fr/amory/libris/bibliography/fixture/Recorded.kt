package fr.amory.libris.bibliography.fixture

fun recorded(name: String): String = resource(name).readText()

fun recordedBytes(name: String): ByteArray = resource(name).readBytes()

private fun resource(name: String) =
  checkNotNull(object {}.javaClass.getResource("/scenarios/$name")) { "no recorded answer $name" }
