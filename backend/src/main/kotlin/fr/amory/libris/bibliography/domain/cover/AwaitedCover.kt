package fr.amory.libris.bibliography.domain.cover

import fr.amory.libris.bibliography.domain.Isbn
import java.time.Duration
import java.time.Instant

private val A_DAY = Duration.ofDays(1)

data class AwaitedCover(val isbn: Isbn, val chosenSource: CoverSource?, val attemptedAt: Instant? = null) {
  fun isDueAt(now: Instant): Boolean =
    attemptedAt == null || attemptedAt.plus(A_DAY) <= now

  fun attempted(at: Instant): AwaitedCover =
    copy(attemptedAt = at)
}
