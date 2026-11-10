package fr.amory.libris.bibliography.domain.series

interface CarriedSeries {
  fun findAll(): Set<SeriesId>
}
