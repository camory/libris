package fr.amory.libris.infra.lookup

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import fr.amory.libris.domain.Source
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Duration

class OpenLibrarySourceTest {
    private val openLibrary = OpenLibrarySource(wireMock.baseUrl(), TIMEOUT)

    @AfterEach
    fun forgetTheStubs() {
        wireMock.resetAll()
    }

    @Test
    fun `the source names itself`() {
        openLibrary.source shouldBe Source.OPEN_LIBRARY
    }

    private companion object {
        val TIMEOUT: Duration = Duration.ofMillis(200)
        val wireMock = WireMockServer(options().dynamicPort())

        @BeforeAll
        @JvmStatic
        fun startWireMock() {
            wireMock.start()
        }

        @AfterAll
        @JvmStatic
        fun stopWireMock() {
            wireMock.stop()
        }
    }
}
