package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.application.cover.FindCover
import fr.amory.libris.fixture.WebSliceTest
import fr.amory.libris.library.application.AddBookToBookshelf
import fr.amory.libris.library.application.FindDefaultBookshelf
import fr.amory.libris.library.application.WelcomeReader
import fr.amory.libris.library.application.catalogue.BrowseCatalogue
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
        BrowseCatalogue::class,
        FindCover::class,
    ],
)
class CoverControllerTest @Autowired constructor(
    private val client: RestTestClient,
    private val welcomeReader: WelcomeReader,
    private val findCover: FindCover,
) {
    @Test
    fun `a malformed name names no cover`() {
        // Given
        given(welcomeReader("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)

        // When
        val body = coverAt("ABC")
            .expectStatus().isNotFound
            .expectBody(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .returnResult().responseBody

        // Then
        body?.get("type") shouldBe "/problems/not-found"
        verifyNoInteractions(findCover)
    }

    private fun coverAt(name: String): RestTestClient.ResponseSpec =
        client.get()
            .uri("/api/v1/covers/{name}", name)
            .headers {
                it.add("Remote-User", "tophe")
                it.add("Remote-Name", "Tophe")
                it.add("Remote-Email", "tophe@amory.fr")
            }
            .exchange()
}
