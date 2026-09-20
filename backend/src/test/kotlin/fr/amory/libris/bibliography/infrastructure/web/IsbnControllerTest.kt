package fr.amory.libris.bibliography.infrastructure.web

import fr.amory.libris.bibliography.application.lookup.LookupEditionByIsbn
import fr.amory.libris.fixture.WebSliceTest
import fr.amory.libris.library.application.ReaderVisit
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
)

@WebSliceTest
@MockitoBean(types = [ReaderVisit::class, LookupEditionByIsbn::class])
class IsbnControllerTest @Autowired constructor(
    private val client: RestTestClient,
    private val visit: ReaderVisit,
    private val lookup: LookupEditionByIsbn,
) {
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

    private fun lookUp(isbn: String): Map<String, Any>? =
        client.get()
            .uri("/api/v1/isbn/{isbn}", isbn)
            .headers {
                it.add("Remote-User", "tophe")
                it.add("Remote-Name", "Tophe")
                it.add("Remote-Email", "tophe@amory.fr")
            }
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .returnResult().responseBody
}
