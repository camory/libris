package fr.amory.libris.bibliography.application.cover

import fr.amory.libris.bibliography.domain.cover.AwaitedCover
import fr.amory.libris.bibliography.domain.cover.AwaitedCoverRepository
import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverFetch
import fr.amory.libris.bibliography.domain.cover.CoverFetches
import fr.amory.libris.bibliography.domain.cover.CoverStore
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations
import java.time.Clock
import java.time.Instant

@Service
class FetchAwaitedCovers(
  private val awaitedCovers: AwaitedCoverRepository,
  coverFetches: List<CoverFetch>,
  private val covers: CoverStore,
  private val editions: EditionRepository,
  private val transactions: TransactionOperations,
  private val clock: Clock) {
  private val coverFetches = CoverFetches.of(coverFetches)

  operator fun invoke() {
    val now = clock.instant()
    awaitedCovers
      .findAll()
      .filter { it.isDueAt(now) }
      .forEach { take(it, now) }
  }

  private fun take(awaitedCover: AwaitedCover, now: Instant) {
    awaitedCovers.update(awaitedCover.attempted(now))
    coverFetches
      .coverFor(awaitedCover.isbn, awaitedCover.chosenSource)
      ?.let { store(awaitedCover, it) }
  }

  private fun store(awaitedCover: AwaitedCover, cover: Cover) {
    covers.write(cover)
    val edition = editions.findByIsbn(awaitedCover.isbn)
    transactions.executeWithoutResult {
      edition?.let { editions.update(it.copy(coverName = cover.name)) }
      awaitedCovers.delete(awaitedCover)
    }
  }
}
