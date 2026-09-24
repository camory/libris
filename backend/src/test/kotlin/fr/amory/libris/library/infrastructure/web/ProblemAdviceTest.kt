package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.fixture.WebSliceTest
import fr.amory.libris.library.application.AddBookToBookshelf
import fr.amory.libris.library.application.FindDefaultBookshelf
import fr.amory.libris.library.application.WelcomeReader
import fr.amory.libris.library.application.lookup.LookupIsbnForReader
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
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

@WebSliceTest
@MockitoBean(
    types = [
        WelcomeReader::class,
        LookupIsbnForReader::class,
        FindDefaultBookshelf::class,
        AddBookToBookshelf::class,
    ],
)
class ProblemAdviceTest @Autowired constructor(
    private val client: RestTestClient,
    private val welcomeReader: WelcomeReader,
    private val addBookToBookshelf: AddBookToBookshelf,
) {
    @Test
    fun `a bookshelf id that is not a uuid is a validation problem`() {
        // Given
        given(welcomeReader("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)

        // When
        val body = add("salon", """{}""")

        // Then
        body?.get("type") shouldBe "/problems/validation"
        body!! shouldNotContainKey "detail"
        verifyNoInteractions(addBookToBookshelf)
    }

    @Test
    fun `a body of the wrong types is a validation problem`() {
        // Given
        given(welcomeReader("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)

        // When
        val body = add(TOPHE.defaultBookshelfId.value.toString(), """"Romance dawn"""")

        // Then
        body?.get("type") shouldBe "/problems/validation"
        body!! shouldNotContainKey "detail"
        verifyNoInteractions(addBookToBookshelf)
    }

    private fun add(bookshelf: String, book: String): Map<String, Any>? =
        client.post()
            .uri("/api/v1/bookshelves/{id}/books", bookshelf)
            .headers {
                it.add("Remote-User", "tophe")
                it.add("Remote-Name", "Tophe")
                it.add("Remote-Email", "tophe@amory.fr")
                it.add("X-Requested-With", "XMLHttpRequest")
            }
            .contentType(APPLICATION_JSON)
            .body(book)
            .exchange()
            .expectStatus().isBadRequest
            .expectBody(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .returnResult().responseBody
}
