package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.application.cover.FindCover
import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.fixture.recordedBytes
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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.client.RestTestClient
import java.util.UUID

private const val NAME = "3f7a9c0e5b2d4816a0c9e7f1b3d5a2c4e6f8091b2c3d4e5f60718293a4b5c6d7"

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
    fun `a stored JPEG is served as a JPEG to be kept a year`() {
        // Given
        val bytes = recordedBytes("covers/tall.jpg")
        given(welcomeReader("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)
        given(findCover(CoverName(NAME))).willReturn(Cover.of(bytes))

        // When
        val body = coverAt(NAME)
            .expectStatus().isOk
            .expectHeader().contentType("image/jpeg")
            .expectHeader().valueEquals("Cache-Control", "public, max-age=31536000, immutable")
            .expectBody(ByteArray::class.java)
            .returnResult().responseBody

        // Then
        body shouldBe bytes
    }

    @Test
    fun `a stored WebP is served as a WebP`() {
        // Given
        val bytes = recordedBytes("covers/small.webp")
        given(welcomeReader("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)
        given(findCover(CoverName(NAME))).willReturn(Cover.of(bytes))

        // When
        val body = coverAt(NAME)
            .expectStatus().isOk
            .expectHeader().contentType("image/webp")
            .expectBody(ByteArray::class.java)
            .returnResult().responseBody

        // Then
        body shouldBe bytes
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
