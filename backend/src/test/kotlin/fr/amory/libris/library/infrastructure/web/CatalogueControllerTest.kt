package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.application.cover.FindCover
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.fixture.WebSliceTest
import fr.amory.libris.library.application.AddBookToBookshelf
import fr.amory.libris.library.application.FindDefaultBookshelf
import fr.amory.libris.library.application.WelcomeReader
import fr.amory.libris.library.application.catalogue.BrowseCatalogue
import fr.amory.libris.library.application.catalogue.CataloguePage
import fr.amory.libris.library.application.catalogue.HeldEdition
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.application.lookup.LookupIsbnForReader
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.copy.CopyId
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

private val ROMANCE_DAWN = HeldEdition(
    edition = Edition(
        id = EditionId(UUID.fromString("01991c3a-5b7e-7c1d-8f2a-3d4e5f6071a1")),
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
    ),
    copies = listOf(
        CopyOnBookshelf(
            copyId = CopyId(UUID.fromString("01991c3a-5b7e-7c1d-8f2a-3d4e5f6071b1")),
            bookshelfId = TOPHE.defaultBookshelfId,
            bookshelfName = "Bibliothèque de Tophe",
        ),
    ),
)

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
class CatalogueControllerTest @Autowired constructor(
    private val client: RestTestClient,
    private val welcomeReader: WelcomeReader,
    private val browseCatalogue: BrowseCatalogue,
) {
    @Test
    fun `a held edition is answered with no cover while none is stored`() {
        // Given
        given(welcomeReader("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)
        given(browseCatalogue(TOPHE.id, null)).willReturn(CataloguePage(listOf(ROMANCE_DAWN), null))

        // When
        val body = client.get()
            .uri("/api/v1/books")
            .headers {
                it.add("Remote-User", "tophe")
                it.add("Remote-Name", "Tophe")
                it.add("Remote-Email", "tophe@amory.fr")
            }
            .exchange()
            .expectStatus().isOk
            .expectBody(object : ParameterizedTypeReference<Map<String, Any?>>() {})
            .returnResult().responseBody

        // Then
        @Suppress("UNCHECKED_CAST")
        val book = (body?.get("books") as List<Map<String, Any?>>).single()
        book["id"] shouldBe "01991c3a-5b7e-7c1d-8f2a-3d4e5f6071a1"
        book["coverUrl"] shouldBe null
    }
}
