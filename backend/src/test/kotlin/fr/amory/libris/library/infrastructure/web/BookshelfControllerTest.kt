package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.fixture.WebSliceTest
import fr.amory.libris.library.application.AddBookResult.NotAnOwner
import fr.amory.libris.library.application.AddBookToBookshelf
import fr.amory.libris.library.application.FindDefaultBookshelf
import fr.amory.libris.library.application.NewBook
import fr.amory.libris.library.application.WelcomeReader
import fr.amory.libris.library.application.lookup.LookupIsbnForReader
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
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.client.RestTestClient
import java.util.UUID

private val TOPHE = readerNamed(
    username = "tophe",
    displayName = "Tophe",
    id = ReaderId(UUID.fromString("01991c3a-5b7e-7c1d-8f2a-3d4e5f607181")),
    defaultBookshelfId = BookshelfId(UUID.fromString("01991c3a-5b7e-7c1d-8f2a-3d4e5f607191")),
)

private val SALON = BookshelfId(UUID.fromString("01991c3a-5b7e-7c1d-8f2a-3d4e5f6071a1"))

private val ROMANCE_DAWN = NewBook(
    isbn = isbnOf("9782723488525"),
    kind = MANGA,
    title = "Romance dawn",
    subtitle = null,
    contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
    series = SeriesEntry("One Piece", 1),
    collection = null,
    publisher = "Glénat",
    publicationYear = 2013,
    language = "fr",
    pageCount = 207,
    summary = null,
    coverUrl = null,
)

private fun romanceDawn(
    isbn13: String = "9782723488525",
    title: String = "Romance dawn",
    author: String = "Eiichirō Oda",
) = """
    {
      "isbn13": "$isbn13",
      "kind": "MANGA",
      "title": "$title",
      "subtitle": null,
      "authors": [{ "name": "$author", "role": "WRITER" }],
      "series": { "name": "One Piece", "volumeNumber": 1 },
      "collection": null,
      "publisher": "Glénat",
      "publicationYear": 2013,
      "language": "fr",
      "pageCount": 207,
      "summary": null,
      "coverUrl": null
    }
"""

@WebSliceTest
@MockitoBean(
    types = [
        WelcomeReader::class,
        LookupIsbnForReader::class,
        FindDefaultBookshelf::class,
        AddBookToBookshelf::class,
    ],
)
class BookshelfControllerTest @Autowired constructor(
    private val client: RestTestClient,
    private val welcomeReader: WelcomeReader,
    private val addBookToBookshelf: AddBookToBookshelf,
) {
    @Test
    fun `a bookshelf the reader sees without owning it is answered as not found`() {
        // Given
        given(welcomeReader("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)
        given(addBookToBookshelf(TOPHE.id, SALON, ROMANCE_DAWN)).willReturn(NotAnOwner)

        // When
        val body = add(SALON, romanceDawn(), NOT_FOUND)

        // Then
        body?.get("type") shouldBe "/problems/not-found"
    }

    @Test
    fun `the thirteen digits with separators are not an isbn13 the add admits`() {
        // Given
        given(welcomeReader("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)

        // When
        val body = add(TOPHE.defaultBookshelfId, romanceDawn(isbn13 = "978-2-7234-8852-5"), BAD_REQUEST)

        // Then
        body?.get("type") shouldBe "/problems/validation"
        verifyNoInteractions(addBookToBookshelf)
    }

    @Test
    fun `a blank title is refused`() {
        // Given
        given(welcomeReader("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)

        // When
        val body = add(TOPHE.defaultBookshelfId, romanceDawn(title = " "), BAD_REQUEST)

        // Then
        body?.get("type") shouldBe "/problems/validation"
        fieldsOf(body) shouldBe listOf("title")
        verifyNoInteractions(addBookToBookshelf)
    }

    @Test
    fun `an author without a name is refused`() {
        // Given
        given(welcomeReader("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)

        // When
        val body = add(TOPHE.defaultBookshelfId, romanceDawn(author = " "), BAD_REQUEST)

        // Then
        body?.get("type") shouldBe "/problems/validation"
        fieldsOf(body) shouldBe listOf("authors")
        verifyNoInteractions(addBookToBookshelf)
    }

    private fun fieldsOf(problem: Map<String, Any>?): List<Any?> =
        (problem?.get("errors") as List<*>).map { (it as Map<*, *>)["field"] }

    private fun add(bookshelf: BookshelfId, book: String, status: HttpStatus): Map<String, Any>? =
        client.post()
            .uri("/api/v1/bookshelves/{id}/books", bookshelf.value)
            .headers {
                it.add("Remote-User", "tophe")
                it.add("Remote-Name", "Tophe")
                it.add("Remote-Email", "tophe@amory.fr")
                it.add("X-Requested-With", "XMLHttpRequest")
            }
            .contentType(APPLICATION_JSON)
            .body(book)
            .exchange()
            .expectStatus().isEqualTo(status)
            .expectBody(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .returnResult().responseBody
}
