package fr.amory.libris.library.domain.bookshelf

import io.kotest.assertions.throwables.shouldThrow
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

  @Test
  fun `a bookshelf name is never blank`() {
    // Given / When / Then
    shouldThrow<IllegalArgumentException> { BookshelfName("   ") }
  }

  @Test
  fun `two names differing only in case and accents are the same`() {
    // Given / When / Then
    BookshelfName("Bibliothèque familiale").isSameAs(BookshelfName("bibliotheque FAMILIALE")) shouldBe true
  }

  @Test
  fun `two different names are not the same`() {
    // Given / When / Then
    BookshelfName("Salon").isSameAs(BookshelfName("Bibliothèque familiale")) shouldBe false
  }

  @Test
  fun `a name written with œ is the same as one written with oe`() {
    // Given / When / Then
    BookshelfName("Œuvres").isSameAs(BookshelfName("oeuvres")) shouldBe true
  }

  @Test
  fun `a name written with æ is the same as one written with ae`() {
    // Given / When / Then
    BookshelfName("Æsop").isSameAs(BookshelfName("aesop")) shouldBe true
  }
}
