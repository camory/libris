package fr.amory.libris.library.domain.bookshelf

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class BookshelfNameTest {
  @Test
  fun `a text that is not blank names a bookshelf`() {
    // Given / When / Then
    BookshelfName.of("Bibliothèque familiale") shouldBe BookshelfName("Bibliothèque familiale")
  }

  @Test
  fun `a blank text names no bookshelf`() {
    // Given / When / Then
    BookshelfName.of("   ") shouldBe null
  }
}
