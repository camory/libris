package fr.amory.libris.web.series

import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.library.application.catalogue.CatalogueSeries
import fr.amory.libris.library.application.catalogue.ListCatalogueSeries
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.web.cover.coverPathOf
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SeriesController(private val listCatalogueSeries: ListCatalogueSeries) {
  @GetMapping("/api/v1/series")
  fun series(@AuthenticationPrincipal reader: Reader): List<SeriesResponse> =
    listCatalogueSeries(reader.id).map { SeriesResponse.from(it) }
}

data class SeriesResponse(
  val name: String,
  val volumeCount: Int,
  val coverUrl: String?,
  val kind: Kind?) {
  companion object {
    fun from(series: CatalogueSeries): SeriesResponse =
      SeriesResponse(
        name = series.name,
        volumeCount = series.volumeCount,
        coverUrl = series.coverName?.let { coverPathOf(it) },
        kind = series.kind,
      )
  }
}
