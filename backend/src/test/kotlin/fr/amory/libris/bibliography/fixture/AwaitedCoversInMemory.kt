package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.cover.AwaitedCover
import fr.amory.libris.bibliography.domain.cover.AwaitedCoverRepository

class AwaitedCoversInMemory : AwaitedCoverRepository {
  private val awaitedCovers = mutableListOf<AwaitedCover>()

  val stored: List<AwaitedCover> get() = awaitedCovers.toList()

  override fun insert(awaitedCover: AwaitedCover) {
    awaitedCovers += awaitedCover
  }
}
