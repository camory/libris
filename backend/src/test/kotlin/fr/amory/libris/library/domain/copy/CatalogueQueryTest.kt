package fr.amory.libris.library.domain.copy

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CatalogueQueryTest {
  @Test
  fun `an empty text is no query`() {
    // Given / When / Then
    CatalogueQuery.of("") shouldBe null
  }

  @Test
  fun `a blank text is no query`() {
    // Given / When / Then
    CatalogueQuery.of("   ") shouldBe null
  }

  @Test
  fun `the constructor refuses a blank text`() {
    // Given / When / Then
    shouldThrow<IllegalArgumentException> { CatalogueQuery("   ") }
  }
}
