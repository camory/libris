package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.application.cover.FindCover
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Held
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.fixture.WebSliceTest
import fr.amory.libris.library.application.AddBookToBookshelf
import fr.amory.libris.library.application.FindDefaultBookshelf
import fr.amory.libris.library.application.WelcomeReader
import fr.amory.libris.library.application.catalogue.BrowseCatalogue
import fr.amory.libris.library.application.lookup.IsbnLookup
import fr.amory.libris.library.application.lookup.LookupIsbnForReader
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
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
)

private val ROMANCE_DAWN_ID = EditionId(UUID.fromString("01991c3a-5b7e-7c1d-8f2a-3d4e5f6071a1"))

@WebSliceTest
@MockitoBean(
    types = [
        WelcomeReader::class,
        LookupIsbnForReader::class,
        FindDefaultBookshelf::class,
        AddBookToBookshelf::class,
        BrowseCatalogue::class,
        FindCover::class,
    ],
)
class IsbnControllerTest @Autowired constructor(
    private val client: RestTestClient,
    private val welcomeReader: WelcomeReader,
    private val lookupIsbnForReader: LookupIsbnForReader,
) {
    @Test
    fun `an edition the house holds is answered with its id`() {
        // Given
        given(welcomeReader("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)
        given(lookupIsbnForReader(TOPHE.id, isbnOf("9782723488525")))
            .willReturn(IsbnLookup(Held(ROMANCE_DAWN_ID, ROMANCE_DAWN), emptyList()))

        // When
        val body = lookUp("9782723488525")

        // Then
        body?.get("id") shouldBe "01991c3a-5b7e-7c1d-8f2a-3d4e5f6071a1"
    }

    private fun lookUp(isbn: String): Map<String, Any>? =
        client.get()
            .uri("/api/v1/isbn/{isbn}", isbn)
            .headers {
                it.add("Remote-User", "tophe")
                it.add("Remote-Name", "Tophe")
                it.add("Remote-Email", "tophe@amory.fr")
            }
            .exchange()
            .expectStatus().isOk
            .expectBody(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .returnResult().responseBody
}
