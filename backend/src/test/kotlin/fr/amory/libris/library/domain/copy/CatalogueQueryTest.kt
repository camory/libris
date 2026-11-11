package fr.amory.libris.library.domain.copy

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CatalogueQueryTest {
  @Test
  fun `an empty text is no query`() {
    // Given / When / Then
    CatalogueQuery.of("") shouldBe null
  }
}
