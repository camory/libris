package fr.amory.libris.bibliography.domain.cover

import fr.amory.libris.bibliography.domain.cover.CoverSource.OPEN_LIBRARY
import fr.amory.libris.bibliography.fixture.isbnOf
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

private const val ONE_PIECE = "9782723488525"
private val NOW = Instant.parse("2026-10-05T08:00:00Z")

class AwaitedCoverTest {
  @Test
  fun `an awaited cover never attempted is due`() {
    AwaitedCover(isbnOf(ONE_PIECE), OPEN_LIBRARY).isDueAt(NOW) shouldBe true
  }

  @Test
  fun `an awaited cover attempted less than a day ago is not due`() {
    // Given
    val attemptedAt = NOW.minus(Duration.ofHours(23).plusMinutes(59))

    // When / Then
    AwaitedCover(isbnOf(ONE_PIECE), OPEN_LIBRARY, attemptedAt).isDueAt(NOW) shouldBe false
  }

  @Test
  fun `an awaited cover attempted a day ago is due again`() {
    // Given
    val attemptedAt = NOW.minus(Duration.ofHours(24))

    // When / Then
    AwaitedCover(isbnOf(ONE_PIECE), OPEN_LIBRARY, attemptedAt).isDueAt(NOW) shouldBe true
  }
}
