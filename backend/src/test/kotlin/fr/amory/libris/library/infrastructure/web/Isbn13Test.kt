package fr.amory.libris.library.infrastructure.web

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class Isbn13Test {
  @Test
  fun `the ten of a book is not an isbn13`() {
    isbn13Of("2723488527") shouldBe null
  }

  @Test
  fun `the thirteen digits followed by a space are not an isbn13`() {
    isbn13Of("9782723488525 ") shouldBe null
  }
}
