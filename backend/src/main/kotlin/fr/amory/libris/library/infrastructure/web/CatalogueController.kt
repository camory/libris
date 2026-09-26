package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.library.application.catalogue.ListCatalogue
import fr.amory.libris.library.domain.catalogue.CatalogueCopy
import fr.amory.libris.library.domain.catalogue.CatalogueEdition
import fr.amory.libris.library.domain.reader.Reader
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

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
class CatalogueController(private val listCatalogue: ListCatalogue) {
    @GetMapping("/api/v1/books")
    fun books(@AuthenticationPrincipal reader: Reader): BookPageResponse =
        BookPageResponse(books = listCatalogue(reader.id).map { bookOf(it) }, next = null)

    private fun bookOf(edition: CatalogueEdition): BookResponse = BookResponse(
        id = edition.editionId.value.toString(),
        isbn13 = edition.isbn?.digits,
        kind = edition.kind,
        title = edition.title,
        subtitle = edition.subtitle,
        authors = edition.contributions.map { IsbnAuthorResponse(it.name, it.role) },
        series = edition.series?.let { IsbnSeriesResponse(it.name, it.volumeNumber) },
        collection = edition.collection,
        publisher = edition.publisher,
        publicationYear = edition.publicationYear,
        language = edition.language,
        pageCount = edition.pageCount,
        summary = edition.summary,
        coverUrl = edition.coverUrl,
        copies = edition.copies.map { copyOf(it) },
    )

    private fun copyOf(copy: CatalogueCopy): CopyResponse = CopyResponse(
        id = copy.copyId.value.toString(),
        bookshelf = BookshelfResponse(copy.bookshelfId.value.toString(), copy.bookshelfName),
    )
}
