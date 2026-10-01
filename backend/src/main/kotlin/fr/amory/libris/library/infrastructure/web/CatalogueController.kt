package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.application.catalogue.BrowseCatalogue
import fr.amory.libris.library.application.catalogue.HeldEdition
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.domain.reader.Reader
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

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
  val copies: List<CopyResponse>,
)

data class BookPageResponse(
  val books: List<BookResponse>,
  val next: String?,
)

@RestController
class CatalogueController(private val browseCatalogue: BrowseCatalogue) {
  @GetMapping("/api/v1/books")
  fun books(
    @AuthenticationPrincipal reader: Reader,
    @RequestParam("after") after: UUID?,
  ): BookPageResponse {
    val page = browseCatalogue(reader.id, after?.let { EditionId(it) })
    return BookPageResponse(books = page.held.map { bookOf(it) }, next = page.next?.value?.toString())
  }

  private fun bookOf(held: HeldEdition): BookResponse = BookResponse(
    id = held.edition.id.value.toString(),
    isbn13 = held.edition.isbn?.digits,
    kind = held.edition.kind,
    title = held.edition.title,
    subtitle = held.edition.subtitle,
    authors = held.edition.contributions.map { IsbnAuthorResponse(it.name, it.role) },
    series = held.edition.series?.let { IsbnSeriesResponse(it.name, it.volumeNumber) },
    collection = held.edition.collection,
    publisher = held.edition.publisher,
    publicationYear = held.edition.publicationYear,
    language = held.edition.language,
    pageCount = held.edition.pageCount,
    summary = held.edition.summary,
    coverUrl = null,
    copies = held.copies.map { copyOf(it) },
  )

  private fun copyOf(copy: CopyOnBookshelf): CopyResponse = CopyResponse(
    id = copy.copyId.value.toString(),
    bookshelf = BookshelfResponse(copy.bookshelfId.value.toString(), copy.bookshelfName),
  )
}
