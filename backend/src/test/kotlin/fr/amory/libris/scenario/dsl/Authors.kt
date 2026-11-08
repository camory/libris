package fr.amory.libris.scenario.dsl

fun Reader.authorNames(): List<String> =
  libris.get(this, "/api/v1/author-names") { read<List<String>>(it) }.value
