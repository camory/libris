package fr.amory.libris.infra.web

import fr.amory.libris.application.IsbnLookup
import fr.amory.libris.application.LookupResult.Found
import fr.amory.libris.application.ReaderVisit
import fr.amory.libris.domain.AuthorRole.ARTIST
import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.Reader
import fr.amory.libris.domain.lookup.Source.BNF
import fr.amory.libris.domain.lookup.Source.OPEN_LIBRARY
import fr.amory.libris.domain.lookup.SourceAuthor
import fr.amory.libris.domain.lookup.SourceEdition
import fr.amory.libris.domain.lookup.SourceSeries
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.client.RestTestClient

private fun isbn13Of(text: String): Isbn13 = checkNotNull(Isbn13.of(text))

private val JULIETTE = Reader(username = "juliette", email = "juliette@amory.fr", displayName = "Juliette")

private val ONE_PIECE_1 = SourceEdition(
    isbn13 = isbn13Of("9782723488525"),
    title = "Romance dawn",
    subtitle = "à l'aube d'une grande aventure",
    authors = listOf(SourceAuthor("Eiichirō Oda", WRITER), SourceAuthor("Eiichirō Oda", ARTIST)),
    series = SourceSeries("One piece", 1),
    collection = "Shonen manga",
    publisher = "Glénat",
    publicationYear = 2013,
    language = "fr",
    pageCount = 203,
    summary = "Luffy prend la mer.",
    coverUrl = "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
)

private val BARE = SourceEdition(
    isbn13 = isbn13Of("9782000000013"),
    title = "Un ouvrage sans rien d'autre",
    subtitle = null,
    authors = emptyList(),
    series = null,
    collection = null,
    publisher = null,
    publicationYear = null,
    language = null,
    pageCount = null,
    summary = null,
    coverUrl = null,
)

@WebSliceTest
@MockitoBean(types = [ReaderVisit::class, IsbnLookup::class])
class IsbnControllerTest @Autowired constructor(
    private val client: RestTestClient,
    private val visit: ReaderVisit,
    private val lookup: IsbnLookup,
) {
    @Test
    fun `an edition every source filled is answered field by field`() {
        // Given
        given(lookup.lookUp(isbn13Of("9782723488525"))).willReturn(Found(ONE_PIECE_1, listOf(BNF, OPEN_LIBRARY)))

        // When
        val response = ask("9782723488525")

        // Then
        response.expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody().json(
                """
                {
                  "isbn13": "9782723488525",
                  "title": "Romance dawn",
                  "subtitle": "à l'aube d'une grande aventure",
                  "authors": [
                    { "name": "Eiichirō Oda", "role": "WRITER" },
                    { "name": "Eiichirō Oda", "role": "ARTIST" }
                  ],
                  "series": { "name": "One piece", "volumeNumber": 1 },
                  "collection": "Shonen manga",
                  "publisher": "Glénat",
                  "publicationYear": 2013,
                  "language": "fr",
                  "pageCount": 203,
                  "summary": "Luffy prend la mer.",
                  "coverUrl": "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
                  "sources": ["BNF", "OPEN_LIBRARY"]
                }
                """,
                JsonCompareMode.STRICT,
            )
    }

    @Test
    fun `an edition the sources left empty carries every field as null`() {
        // Given
        given(lookup.lookUp(isbn13Of("9782000000013"))).willReturn(Found(BARE, listOf(OPEN_LIBRARY)))

        // When
        val response = ask("9782000000013")

        // Then
        response.expectStatus().isOk()
            .expectBody().json(
                """
                {
                  "isbn13": "9782000000013",
                  "title": "Un ouvrage sans rien d'autre",
                  "subtitle": null,
                  "authors": [],
                  "series": null,
                  "collection": null,
                  "publisher": null,
                  "publicationYear": null,
                  "language": null,
                  "pageCount": null,
                  "summary": null,
                  "coverUrl": null,
                  "sources": ["OPEN_LIBRARY"]
                }
                """,
                JsonCompareMode.STRICT,
            )
    }

    @Test
    fun `an ISBN with a wrong check digit is refused`() {
        // When
        val response = ask("9782723488526")

        // Then
        response.expectStatus().isBadRequest()
            .expectHeader().contentType(APPLICATION_PROBLEM_JSON)
            .expectBody().json(
                """
                {
                  "type": "/problems/validation",
                  "title": "Bad Request",
                  "status": 400,
                  "errors": [{ "field": "isbn", "code": "not-an-isbn" }]
                }
                """,
            )
    }

    @Test
    fun `a text that is not digits at all never reaches the lookup`() {
        // When
        val response = ask("pas-un-isbn")

        // Then
        response.expectStatus().isBadRequest()
            .expectHeader().contentType(APPLICATION_PROBLEM_JSON)
            .expectBody().jsonPath("$.errors").isEqualTo(listOf(mapOf("field" to "isbn", "code" to "not-an-isbn")))
        verifyNoInteractions(lookup)
    }

    private fun ask(isbn: String): RestTestClient.ResponseSpec {
        given(visit.visit("juliette", "juliette@amory.fr", "Juliette")).willReturn(JULIETTE)
        return client.get()
            .uri("/api/v1/isbn/$isbn")
            .headers { it.putAll(READER) }
            .accept(APPLICATION_JSON, APPLICATION_PROBLEM_JSON)
            .exchange()
    }

    private companion object {
        val READER = mapOf(
            "Remote-User" to listOf("juliette"),
            "Remote-Name" to listOf("Juliette"),
            "Remote-Email" to listOf("juliette@amory.fr"),
        )
    }
}
