package fr.amory.libris.bibliography.infrastructure.lookup

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import fr.amory.libris.bibliography.domain.lookup.CoverCandidate
import fr.amory.libris.bibliography.domain.lookup.Source.INVENTAIRE
import fr.amory.libris.bibliography.fixture.InventaireStubs
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.bibliography.fixture.recordedBytes
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds

class InventaireCoverLookupTest {
    private val source = InventaireCoverLookup(server.baseUrl(), TIMEOUT)

    @AfterEach
    fun forgetTheStubs() {
        server.resetAll()
    }

    @Test
    fun `a known ISBN offers inventaire io's picture, at most 600 tall`() {
        // Given
        inventaire.knows(ONE_PIECE_1, SMALL_WEBP)

        // When
        val candidate = source.lookUp(isbnOf(ONE_PIECE_1))

        // Then
        candidate shouldBe CoverCandidate(
            INVENTAIRE,
            "${server.baseUrl()}/img/entities/100x600/34d6e7d99cec5b0922b9eccfeb03748ab2b4db99",
        )
    }

    @Test
    fun `looking up an ISBN asks for its entity alone, never for the picture`() {
        // Given
        inventaire.knows(ONE_PIECE_1, SMALL_WEBP)

        // When
        source.lookUp(isbnOf(ONE_PIECE_1))

        // Then
        val request = server.allServeEvents.map { it.request }.single()
        request.url.substringBefore("?") shouldBe "/api/entities"
        request.queryParameter("action").values() shouldBe listOf("by-uris")
        request.queryParameter("uris").values() shouldBe listOf("isbn:$ONE_PIECE_1")
        inventaire.pictureRequests() shouldBe 0
    }

    @Test
    fun `an ISBN inventaire io does not know offers no picture`() {
        // Given
        inventaire.doesNotKnow(LES_NERONIA)

        // When
        val candidate = source.lookUp(isbnOf(LES_NERONIA))

        // Then
        candidate.shouldBeNull()
    }

    @Test
    fun `an entity without a picture offers none`() {
        // Given
        inventaire.knowsWithoutPicture(ONE_PIECE_1)

        // When
        val candidate = source.lookUp(isbnOf(ONE_PIECE_1))

        // Then
        candidate.shouldBeNull()
    }

    @Test
    fun `an entity whose picture cannot be read offers none`() {
        // Given
        inventaire.knowsWithPictureClaim(ONE_PIECE_1, mapOf("v" to 1))

        // When
        val candidate = source.lookUp(isbnOf(ONE_PIECE_1))

        // Then
        candidate.shouldBeNull()
    }

    @Test
    fun `an entity whose picture is null offers none`() {
        // Given
        inventaire.knowsWithPictureClaim(ONE_PIECE_1, null)

        // When
        val candidate = source.lookUp(isbnOf(ONE_PIECE_1))

        // Then
        candidate.shouldBeNull()
    }

    @Test
    fun `an entity whose picture is empty offers none`() {
        // Given
        inventaire.knowsWithPictureClaim(ONE_PIECE_1, "")

        // When
        val candidate = source.lookUp(isbnOf(ONE_PIECE_1))

        // Then
        candidate.shouldBeNull()
    }

    @Test
    fun `an inventaire io that fails offers no picture`() {
        // Given
        inventaire.fails()

        // When
        val candidate = source.lookUp(isbnOf(ONE_PIECE_1))

        // Then
        candidate.shouldBeNull()
    }

    @Test
    fun `an inventaire io that answers past the timeout offers no picture`() {
        // Given
        inventaire.answersTooLate(ONE_PIECE_1)

        // When
        val candidate = source.lookUp(isbnOf(ONE_PIECE_1))

        // Then
        candidate.shouldBeNull()
    }

    private companion object {
        const val ONE_PIECE_1 = "9782723488525"
        const val LES_NERONIA = "9782505125990"
        val SMALL_WEBP = recordedBytes("covers/small.webp")
        val TIMEOUT: Duration = ofMillis(200)
        val WARM_UP_TIMEOUT: Duration = ofSeconds(20)
        val server = WireMockServer(options().dynamicPort())
        val inventaire = InventaireStubs(server)

        @BeforeAll
        @JvmStatic
        fun startWireMock() {
            server.start()
            inventaire.knows(ONE_PIECE_1, SMALL_WEBP)
            InventaireCoverLookup(server.baseUrl(), WARM_UP_TIMEOUT).lookUp(isbnOf(ONE_PIECE_1))
            server.resetAll()
        }

        @AfterAll
        @JvmStatic
        fun stopWireMock() {
            server.stop()
        }
    }
}
