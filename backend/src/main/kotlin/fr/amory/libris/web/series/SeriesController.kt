package fr.amory.libris.web.series

import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesId
import fr.amory.libris.library.application.catalogue.CatalogueSeries
import fr.amory.libris.library.application.catalogue.FindCatalogueSeries
import fr.amory.libris.library.application.catalogue.FindCatalogueSeriesResult.Found
import fr.amory.libris.library.application.catalogue.FindCatalogueSeriesResult.NoSuchSeries
import fr.amory.libris.library.application.catalogue.ListCatalogueSeries
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.web.catalogue.BookResponse
import fr.amory.libris.web.cover.coverPathOf
import fr.amory.libris.web.problem.NOT_FOUND_PROBLEM
import fr.amory.libris.web.problem.asResponse
import fr.amory.libris.web.problem.problem
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class SeriesController(
  private val listCatalogueSeries: ListCatalogueSeries,
  private val findCatalogueSeries: FindCatalogueSeries) {
  @GetMapping("/api/v1/series")
  fun series(@AuthenticationPrincipal reader: Reader): List<SeriesResponse> =
    listCatalogueSeries(reader.id).map { SeriesResponse.from(it) }

  @GetMapping("/api/v1/series/{id}")
  fun find(@AuthenticationPrincipal reader: Reader, @PathVariable("id") id: UUID): ResponseEntity<Any> =
    when (val result = findCatalogueSeries(reader.id, SeriesId(id))) {
      NoSuchSeries -> problem(NOT_FOUND, NOT_FOUND_PROBLEM).asResponse()
      is Found     -> ResponseEntity.status(OK).body(SeriesWithBooksResponse.from(result))
    }
}

data class SeriesResponse(
  val id: String,
  val name: String,
  val volumeCount: Int,
  val coverUrl: String?,
  val kind: Kind?) {
  companion object {
    fun from(series: CatalogueSeries): SeriesResponse =
      SeriesResponse(
        id = series.id.value.toString(),
        name = series.name,
        volumeCount = series.volumeCount,
        coverUrl = series.coverName?.let { coverPathOf(it) },
        kind = series.kind,
      )
  }
}

data class SeriesWithBooksResponse(
  val id: String,
  val name: String,
  val volumeCount: Int,
  val coverUrl: String?,
  val kind: Kind?,
  val books: List<BookResponse>) {
  companion object {
    fun from(found: Found): SeriesWithBooksResponse =
      SeriesWithBooksResponse(
        id = found.series.id.value.toString(),
        name = found.series.name,
        volumeCount = found.series.volumeCount,
        coverUrl = found.series.coverName?.let { coverPathOf(it) },
        kind = found.series.kind,
        books = found.books.map { BookResponse.from(it) },
      )
  }
}
