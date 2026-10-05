package fr.amory.libris.bibliography.domain.cover

import fr.amory.libris.bibliography.domain.Isbn
import java.time.Instant

data class AwaitedCover(val isbn: Isbn, val chosenSource: CoverSource?, val attemptedAt: Instant? = null) {
  fun isDueAt(now: Instant): Boolean =
    attemptedAt?.isBefore(now) ?: true
}
