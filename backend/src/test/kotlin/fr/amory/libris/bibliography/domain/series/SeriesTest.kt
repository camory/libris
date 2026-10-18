package fr.amory.libris.bibliography.domain.series

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class SeriesTest {
  @Test
  fun `a series without a name is refused`() {
    shouldThrow<IllegalArgumentException> { Series(SeriesId.new(), " ") }
  }
}
