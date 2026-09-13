package fr.amory.libris.infra.lookup

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.github.tomakehurst.wiremock.http.RequestMethod.GET
import fr.amory.libris.fixture.BnfStubs
import fr.amory.libris.fixture.isbn13Of
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds

class BnfSourceTest {
    private val source = BnfSource(server.baseUrl() + SRU, TIMEOUT)

    @AfterEach
    fun forgetTheStubs() {
        server.resetAll()
    }

    @Test
    fun `looking up an ISBN sends one search on the SRU endpoint`() {
        // Given
        bnf.knows(ONE_PIECE)

        // When
        source.lookUp(isbn13Of(ONE_PIECE))

        // Then
        val request = server.allServeEvents.map { it.request }.single()
        request.method shouldBe GET
        request.url.substringBefore("?") shouldBe SRU
        request.queryParameter("version").values() shouldBe listOf("1.2")
        request.queryParameter("operation").values() shouldBe listOf("searchRetrieve")
        request.queryParameter("recordSchema").values() shouldBe listOf("unimarcxchange")
        request.queryParameter("maximumRecords").values() shouldBe listOf("1")
        request.queryParameter("query").values() shouldBe listOf("""bib.isbn all "$ONE_PIECE"""")
    }

    private companion object {
        const val ONE_PIECE = "9782723488525"
        const val SRU = "/api/SRU"
        val TIMEOUT: Duration = ofMillis(200)
        val WARM_UP_TIMEOUT: Duration = ofSeconds(20)
        val server = WireMockServer(options().dynamicPort())
        val bnf = BnfStubs(server)

        @BeforeAll
        @JvmStatic
        fun startWireMock() {
            server.start()
            bnf.knows(ONE_PIECE)
            BnfSource(server.baseUrl() + SRU, WARM_UP_TIMEOUT).lookUp(isbn13Of(ONE_PIECE))
            server.resetAll()
        }

        @AfterAll
        @JvmStatic
        fun stopWireMock() {
            server.stop()
        }
    }
}
