package fr.amory.libris.scenario.dsl

data class Series(val id: String?, val name: String)

data class SeriesWithBooks(val name: String, val books: List<Book>)

fun Reader.series(): List<Series> =
  libris.get(this, "/api/v1/series") { read<List<Series>>(it) }.value

fun Reader.opens(series: Series): Answer<SeriesWithBooks> =
  libris.get(this, "/api/v1/series/${series.id}") { read<SeriesWithBooks>(it) }
