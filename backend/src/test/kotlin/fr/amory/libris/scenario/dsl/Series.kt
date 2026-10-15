package fr.amory.libris.scenario.dsl

data class Series(val name: String)

fun Reader.series(): List<Series> =
  libris.get(this, "/api/v1/series") { read<List<Series>>(it) }.value
