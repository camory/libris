package fr.amory.libris.bibliography.domain.cover

interface AwaitedCoverRepository {
  fun insert(awaitedCover: AwaitedCover)
}
