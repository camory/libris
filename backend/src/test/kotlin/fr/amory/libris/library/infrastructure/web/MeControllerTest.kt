package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.fixture.WebSliceTest
import fr.amory.libris.library.application.AddBookToBookshelf
import fr.amory.libris.library.application.FindDefaultBookshelf
import fr.amory.libris.library.application.WelcomeReader
import fr.amory.libris.library.application.lookup.LookupIsbnForReader
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.bookshelfOwnedBy
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
private val JULIETTE = readerNamed(
    username = "juliette",
    displayName = "Juliette",
    id = ReaderId(UUID.fromString("01991c3a-5b7e-7c1d-8f2a-3d4e5f607182")),
    defaultBookshelfId = BookshelfId(UUID.fromString("01991c3a-5b7e-7c1d-8f2a-3d4e5f607192")),
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
class MeControllerTest @Autowired constructor(
    private val client: RestTestClient,
    private val welcomeReader: WelcomeReader,
    private val findDefaultBookshelf: FindDefaultBookshelf,
) {
    @Test
    fun `a reader of the admin group is an admin`() {
        // Given
        given(welcomeReader("tophe", "tophe@amory.fr", "Tophe")).willReturn(TOPHE)
        given(findDefaultBookshelf(TOPHE)).willReturn(bookshelfOwnedBy(TOPHE))
        val headers = listOf(
            "Remote-User" to "tophe",
            "Remote-Name" to "Tophe",
            "Remote-Email" to "tophe@amory.fr",
            "Remote-Groups" to "family,libris-admin",
        )

        // When
        val body = me(headers)

        // Then
        body shouldBe mapOf(
            "id" to "01991c3a-5b7e-7c1d-8f2a-3d4e5f607181",
            "username" to "tophe",
            "displayName" to "Tophe",
            "email" to "tophe@amory.fr",
            "role" to "ADMIN",
            "defaultBookshelf" to mapOf(
                "id" to "01991c3a-5b7e-7c1d-8f2a-3d4e5f607191",
                "name" to "Bibliothèque de Tophe",
            ),
        )
    }

    @Test
    fun `a reader outside the admin group is a plain reader`() {
        // Given
        given(welcomeReader("juliette", "juliette@amory.fr", "Juliette")).willReturn(JULIETTE)
        given(findDefaultBookshelf(JULIETTE)).willReturn(bookshelfOwnedBy(JULIETTE))
        val headers = listOf(
            "Remote-User" to "juliette",
            "Remote-Name" to "Juliette",
            "Remote-Email" to "juliette@amory.fr",
            "Remote-Groups" to "family",
        )

        // When
        val body = me(headers)

        // Then
        body shouldBe mapOf(
            "id" to "01991c3a-5b7e-7c1d-8f2a-3d4e5f607182",
            "username" to "juliette",
            "displayName" to "Juliette",
            "email" to "juliette@amory.fr",
            "role" to "READER",
            "defaultBookshelf" to mapOf(
                "id" to "01991c3a-5b7e-7c1d-8f2a-3d4e5f607192",
                "name" to "Bibliothèque de Juliette",
            ),
        )
    }

    @Test
    fun `a reader without any group is a plain reader`() {
        // Given
        given(welcomeReader("juliette", "juliette@amory.fr", "Juliette")).willReturn(JULIETTE)
        given(findDefaultBookshelf(JULIETTE)).willReturn(bookshelfOwnedBy(JULIETTE))
        val headers = listOf(
            "Remote-User" to "juliette",
            "Remote-Name" to "Juliette",
            "Remote-Email" to "juliette@amory.fr",
        )

        // When
        val body = me(headers)

        // Then
        body?.get("role") shouldBe "READER"
    }

    @Test
    fun `a reader without a display name visits under their username`() {
        // Given
        val juliette = JULIETTE.copy(displayName = "juliette")
        given(welcomeReader("juliette", "juliette@amory.fr", "juliette")).willReturn(juliette)
        given(findDefaultBookshelf(juliette)).willReturn(bookshelfOwnedBy(juliette))
        val headers = listOf(
            "Remote-User" to "juliette",
            "Remote-Email" to "juliette@amory.fr",
            "Remote-Groups" to "family",
        )

        // When
        val body = me(headers)

        // Then
        body?.get("displayName") shouldBe "juliette"
    }

    @Test
    fun `a reader whose display name is blank visits under their username`() {
        // Given
        val juliette = JULIETTE.copy(displayName = "juliette")
        given(welcomeReader("juliette", "juliette@amory.fr", "juliette")).willReturn(juliette)
        given(findDefaultBookshelf(juliette)).willReturn(bookshelfOwnedBy(juliette))
        val headers = listOf(
            "Remote-User" to "juliette",
            "Remote-Name" to " ",
            "Remote-Email" to "juliette@amory.fr",
            "Remote-Groups" to "family",
        )

        // When
        val body = me(headers)

        // Then
        body?.get("displayName") shouldBe "juliette"
    }

    @Test
    fun `the answer is the reader of the visit, not the one of the headers`() {
        // Given
        given(welcomeReader("juliette", "juju@amory.fr", "Juju")).willReturn(JULIETTE)
        given(findDefaultBookshelf(JULIETTE)).willReturn(bookshelfOwnedBy(JULIETTE))
        val headers = listOf(
            "Remote-User" to "juliette",
            "Remote-Name" to "Juju",
            "Remote-Email" to "juju@amory.fr",
            "Remote-Groups" to "family",
        )

        // When
        val body = me(headers)

        // Then
        body shouldBe mapOf(
            "id" to "01991c3a-5b7e-7c1d-8f2a-3d4e5f607182",
            "username" to "juliette",
            "displayName" to "Juliette",
            "email" to "juliette@amory.fr",
            "role" to "READER",
            "defaultBookshelf" to mapOf(
                "id" to "01991c3a-5b7e-7c1d-8f2a-3d4e5f607192",
                "name" to "Bibliothèque de Juliette",
            ),
        )
    }

    private fun me(headers: List<Pair<String, String>>): Map<String, Any>? =
        client.get()
            .uri("/api/v1/me")
            .headers { headers.forEach { (name, value) -> it.add(name, value) } }
            .exchange()
            .expectStatus().isOk()
            .expectBody(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .returnResult().responseBody
}
