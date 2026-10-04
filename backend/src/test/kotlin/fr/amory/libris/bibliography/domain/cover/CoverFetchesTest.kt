package fr.amory.libris.bibliography.domain.cover

import fr.amory.libris.bibliography.domain.cover.CoverSource.BNF
import fr.amory.libris.bibliography.domain.cover.CoverSource.INVENTAIRE
import fr.amory.libris.bibliography.domain.cover.CoverSource.OPEN_LIBRARY
import fr.amory.libris.bibliography.fixture.CoverFetchAnswering
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CoverFetchesTest {
  private val inventaire = CoverFetchAnswering(INVENTAIRE, null)
  private val openLibrary = CoverFetchAnswering(OPEN_LIBRARY, null)
  private val bnf = CoverFetchAnswering(BNF, null)

  @Test
  fun `with a chosen source, its fetch alone is asked`() {
    // Given
    val coverFetches = CoverFetches.of(listOf(inventaire, openLibrary, bnf))

    // When
    val asked = coverFetches.askedFor(OPEN_LIBRARY)

    // Then
    asked shouldBe listOf(openLibrary)
  }
}
