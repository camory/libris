package fr.amory.libris.bibliography.domain.lookup

import fr.amory.libris.bibliography.domain.lookup.CoverSource.BNF
import fr.amory.libris.bibliography.domain.lookup.CoverSource.INVENTAIRE
import fr.amory.libris.bibliography.domain.lookup.CoverSource.OPEN_LIBRARY
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CoverCandidatesTest {
  @Test
  fun `the candidates are in the house's order, inventaire io, Open Library, then the BnF`() {
    // Given
    val given = listOf(
      CoverCandidate(BNF, "https://catalogue.bnf.fr/couverture"),
      CoverCandidate(INVENTAIRE, "https://inventaire.io/img/entities/100x600/34d6e7d9"),
      CoverCandidate(OPEN_LIBRARY, "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg"),
    )

    // When
    val candidates = CoverCandidates.of(given)

    // Then
    candidates.toList() shouldBe listOf(
      CoverCandidate(INVENTAIRE, "https://inventaire.io/img/entities/100x600/34d6e7d9"),
      CoverCandidate(OPEN_LIBRARY, "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg"),
      CoverCandidate(BNF, "https://catalogue.bnf.fr/couverture"),
    )
  }
}
