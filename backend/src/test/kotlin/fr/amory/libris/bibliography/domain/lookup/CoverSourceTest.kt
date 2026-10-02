package fr.amory.libris.bibliography.domain.lookup

import fr.amory.libris.bibliography.domain.lookup.CoverSource.BNF
import fr.amory.libris.bibliography.domain.lookup.CoverSource.INVENTAIRE
import fr.amory.libris.bibliography.domain.lookup.CoverSource.OPEN_LIBRARY
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CoverSourceTest {
  @Test
  fun `each source is found by its name`() {
    CoverSource.of("inventaire.io") shouldBe INVENTAIRE
    CoverSource.of("Open Library") shouldBe OPEN_LIBRARY
    CoverSource.of("BnF") shouldBe BNF
  }

  @Test
  fun `the house's own name is no source`() {
    CoverSource.of("Libris") shouldBe null
  }
}
