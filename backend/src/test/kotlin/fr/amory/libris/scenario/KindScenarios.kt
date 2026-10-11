package fr.amory.libris.scenario

import com.github.tomakehurst.wiremock.WireMockServer
import fr.amory.libris.bibliography.fixture.BnfStubs
import fr.amory.libris.bibliography.fixture.OpenLibraryStubs
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.test.web.servlet.client.RestTestClient

@ScenarioTest
class KindScenarios @Autowired constructor(
  http: RestTestClient,
  @Qualifier("bnf") bnfServer: WireMockServer,
  @Qualifier("openLibrary") openLibraryServer: WireMockServer) {
  private val libris = Libris(http)
  private val bnf = BnfStubs(bnfServer)
  private val openLibrary = OpenLibraryStubs(openLibraryServer)

  @Test
  fun `S1 A manga`() {
    // Given
    bnf.knows("9782723488525")
    openLibrary.doesNotKnow("9782723488525")
    // When
    val response = libris.ask(JULIETTE, "9782723488525")
    // Then
    response.expectStatus().isOk()
      .expectBody().jsonPath("$.kind").isEqualTo("MANGA")
  }

  @Test
  fun `S2 A BD`() {
    // Given
    bnf.knows("9782505083399")
    openLibrary.doesNotKnow("9782505083399")
    // When
    val response = libris.ask(JULIETTE, "9782505083399")
    // Then
    response.expectStatus().isOk()
      .expectBody().jsonPath("$.kind").isEqualTo("BD")
  }

  @Test
  fun `S3 A book`() {
    // Given
    bnf.knows("9782371025219")
    openLibrary.doesNotKnow("9782371025219")
    // When
    val response = libris.ask(JULIETTE, "9782371025219")
    // Then
    response.expectStatus().isOk()
      .expectBody().jsonPath("$.kind").isEqualTo("BOOK")
  }

  @Test
  fun `S4 No record says`() {
    // Given
    bnf.doesNotKnow("9782380751673")
    openLibrary.knows("9782380751673")
    // When
    val response = libris.ask(JULIETTE, "9782380751673")
    // Then
    response.expectStatus().isOk()
      .expectBody().jsonPath("$.kind").isEqualTo("BOOK")
  }

  private companion object {
    val JULIETTE = Reader("juliette", "Juliette")
  }
}
