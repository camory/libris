package fr.amory.libris.bibliography.domain.series

import fr.amory.libris.bibliography.domain.SeriesEntry
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SeriesVolumeTest {
  @Test
  fun `a volume enters its series under the series' name and its number`() {
    // Given
    val onePiece = Series(SeriesId.new(), "One Piece")
    val volume = SeriesVolume(onePiece.id, 3)

    // When
    val entry = volume.entryIn(onePiece)

    // Then
    entry shouldBe SeriesEntry("One Piece", 3)
  }

  @Test
  fun `a volume does not enter another series`() {
    // Given
    val volume = SeriesVolume(SeriesId.new(), 3)
    val tintin = Series(SeriesId.new(), "Tintin")

    // When / Then
    shouldThrow<IllegalArgumentException> { volume.entryIn(tintin) }
  }
}
