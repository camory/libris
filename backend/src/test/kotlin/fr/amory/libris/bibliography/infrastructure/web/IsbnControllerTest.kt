package fr.amory.libris.bibliography.infrastructure.web

import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Held
import fr.amory.libris.bibliography.application.lookup.LookupEditionByIsbn
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.fixture.WebSliceTest
import fr.amory.libris.library.application.ReaderVisit
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.OK
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.client.RestTestClient
import java.util.UUID

private val TOPHE = readerNamed(
    username = "tophe",
    displayName = "Tophe",
    id = ReaderId(UUID.fromString("01991c3a-5b7e-7c1d-8f2a-3d4e5f607181")),
    defaultBookshelfId = BookshelfId(UUID.fromString("01991c3a-5b7e-7c1d-8f2a-3d4e5f607191")),
)

private val ROMANCE_DAWN = EditionPreview(
    isbn = isbnOf("9782723488525"),
    kind = MANGA,
    title = "Romance dawn",
    subtitle = "Tome 01",
    contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
    series = SeriesEntry("One Piece", 1),
    collection = "Shōnen",
    publisher = "Glénat",
    publicationYear = 2013,
    language = "fr",
    pageCount = 207,
    summary = "Luffy rêve de devenir le roi des pirates.",
    coverUrl = "https://couvertures.amory.fr/one-piece-01.jpg",
)

@WebSliceTest
@MockitoBean(types = [ReaderVisit::class, LookupEditionByIsbn::class])
class IsbnControllerTest @Autowired constructor(
    private val client: RestTestClient,
    private val visit: ReaderVisit,
    private val lookup: LookupEditionByIsbn,
) {
    @Test
    fun `an edition the house holds is answered with its fields`() {
        // Given
        given(visit.visit("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)
        given(lookup.lookUp(isbnOf("9782723488525"))).willReturn(Held(EditionId.new(), ROMANCE_DAWN))

        // When
        val body = lookUp("9782723488525", OK)

        // Then
        body shouldBe mapOf(
            "isbn13" to "9782723488525",
            "kind" to "MANGA",
            "title" to "Romance dawn",
            "subtitle" to "Tome 01",
            "authors" to listOf(mapOf("name" to "Eiichirō Oda", "role" to "WRITER")),
            "series" to mapOf("name" to "One Piece", "volumeNumber" to 1),
            "collection" to "Shōnen",
            "publisher" to "Glénat",
            "publicationYear" to 2013,
            "language" to "fr",
            "pageCount" to 207,
            "summary" to "Luffy rêve de devenir le roi des pirates.",
            "coverUrl" to "https://couvertures.amory.fr/one-piece-01.jpg",
        )
    }

    @Test
    fun `the ten of a book is not an ISBN the API admits`() {
        // Given
        given(visit.visit("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)

        // When
        val body = lookUp("2723488527")

        // Then
        body?.get("type") shouldBe "/problems/validation"
        verifyNoInteractions(lookup)
    }

    @Test
    fun `the thirteen digits with separators are not the writing the API admits`() {
        // Given
        given(visit.visit("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)

        // When
        val body = lookUp("978-2-7234-8852-5")

        // Then
        body?.get("type") shouldBe "/problems/validation"
        verifyNoInteractions(lookup)
    }

    @Test
    fun `the thirteen digits followed by a space are not the writing the API admits`() {
        // Given
        given(visit.visit("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)

        // When
        val body = lookUp("9782723488525 ")

        // Then
        body?.get("type") shouldBe "/problems/validation"
        verifyNoInteractions(lookup)
    }

    @Test
    fun `a thirteen-digit EAN that is not an ISBN is refused`() {
        // Given
        given(visit.visit("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)

        // When
        val body = lookUp("4006381333931")

        // Then
        body?.get("type") shouldBe "/problems/validation"
        verifyNoInteractions(lookup)
    }

    private fun lookUp(isbn: String, status: HttpStatus = BAD_REQUEST): Map<String, Any>? =
        client.get()
            .uri("/api/v1/isbn/{isbn}", isbn)
            .headers {
                it.add("Remote-User", "tophe")
                it.add("Remote-Name", "Tophe")
                it.add("Remote-Email", "tophe@amory.fr")
            }
            .exchange()
            .expectStatus().isEqualTo(status)
            .expectBody(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .returnResult().responseBody
}
