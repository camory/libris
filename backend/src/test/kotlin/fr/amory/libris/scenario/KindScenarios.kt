package fr.amory.libris.scenario

import fr.amory.libris.bibliography.fixture.BnfStubs
import fr.amory.libris.bibliography.fixture.OpenLibraryStubs
import fr.amory.libris.scenario.dsl.Libris
import fr.amory.libris.scenario.dsl.asks
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@ScenarioTest
class KindScenarios @Autowired constructor(
  libris: Libris,
  private val bnf: BnfStubs,
  private val openLibrary: OpenLibraryStubs) {
  private val juliette = libris.reader("juliette", "Juliette")

  @Test
  fun `S1 A manga`() {
    // Given
    bnf.knows("9782723488525")
    openLibrary.doesNotKnow("9782723488525")

    // When
    val answer = juliette.asks("9782723488525")

    // Then
    answer.value.kind shouldBe "MANGA"
  }

  @Test
  fun `S2 A BD`() {
    // Given
    bnf.knows("9782505083399")
    openLibrary.doesNotKnow("9782505083399")

    // When
    val answer = juliette.asks("9782505083399")

    // Then
    answer.value.kind shouldBe "BD"
  }

  @Test
  fun `S3 A book`() {
    // Given
    bnf.knows("9782371025219")
    openLibrary.doesNotKnow("9782371025219")

    // When
    val answer = juliette.asks("9782371025219")

    // Then
    answer.value.kind shouldBe "BOOK"
  }

  @Test
  fun `S4 No record says`() {
    // Given
    bnf.doesNotKnow("9782380751673")
    openLibrary.knows("9782380751673")

    // When
    val answer = juliette.asks("9782380751673")

    // Then
    answer.value.kind shouldBe "BOOK"
  }
}
