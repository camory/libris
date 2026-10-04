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

@Service
class FetchAwaitedCovers(
  private val awaitedCovers: AwaitedCoverRepository,
  coverFetches: List<CoverFetch>,
  private val covers: CoverStore,
  private val editions: EditionRepository,
  private val transactions: TransactionOperations) {
  private val coverFetches = CoverFetches.of(coverFetches)

  operator fun invoke() {
    awaitedCovers
      .findAll()
      .forEach { take(it) }
  }

  private fun take(awaitedCover: AwaitedCover) {
    coverFetches
      .askedFor(awaitedCover.chosenSource)
      .firstNotNullOfOrNull { it.fetch(awaitedCover.isbn) }
      ?.let { store(awaitedCover, it.normalised()) }
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
