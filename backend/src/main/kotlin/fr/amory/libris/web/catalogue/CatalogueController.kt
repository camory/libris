package fr.amory.libris.web.catalogue

import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.application.catalogue.BrowseCatalogue
import fr.amory.libris.library.application.catalogue.CataloguePage
import fr.amory.libris.library.application.catalogue.HeldEdition
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.web.bookshelf.CopyResponse
import fr.amory.libris.web.cover.coverPathOf
import fr.amory.libris.web.isbn.IsbnAuthorResponse
import fr.amory.libris.web.isbn.IsbnSeriesResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class CatalogueController(private val browseCatalogue: BrowseCatalogue) {
  @GetMapping("/api/v1/books")
  fun books(@AuthenticationPrincipal reader: Reader, @RequestParam("after") after: UUID?): BookPageResponse =
    BookPageResponse.from(browseCatalogue(reader.id, after?.let { EditionId(it) }))
}

data class BookPageResponse(
  val books: List<BookResponse>,
  val next: String?) {
  companion object {
    fun from(page: CataloguePage): BookPageResponse =
      BookPageResponse(books = page.held.map { BookResponse.from(it) }, next = page.next?.value?.toString())
  }
}

data class BookResponse(
  val id: String,
  val isbn13: String?,
  val kind: Kind,
  val title: String,
  val subtitle: String?,
  val authors: List<IsbnAuthorResponse>,
  val series: IsbnSeriesResponse?,
  val collection: String?,
  val publisher: String?,
  val publicationYear: Int?,
  val language: String?,
  val pageCount: Int?,
  val summary: String?,
  val coverUrl: String?,
  val copies: List<CopyResponse>) {
  companion object {
    fun from(held: HeldEdition): BookResponse =
      BookResponse(
        id = held.edition.id.value.toString(),
        isbn13 = held.edition.isbn?.digits,
        kind = held.edition.kind,
        title = held.edition.title,
        subtitle = held.edition.subtitle,
        authors = held.edition.contributions.map { IsbnAuthorResponse.from(it) },
        series = held.edition.series?.let { IsbnSeriesResponse.from(it) },
        collection = held.edition.collection,
        publisher = held.edition.publisher,
        publicationYear = held.edition.publicationYear,
        language = held.edition.language,
        pageCount = held.edition.pageCount,
        summary = held.edition.summary,
        coverUrl = held.edition.coverName?.let { coverPathOf(it) },
        copies = held.copies.map { CopyResponse.from(it) },
      )
  }
}
