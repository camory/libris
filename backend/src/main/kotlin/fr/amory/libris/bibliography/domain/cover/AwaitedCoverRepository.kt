package fr.amory.libris.bibliography.domain.cover

interface AwaitedCoverRepository {
  fun insert(awaitedCover: AwaitedCover)

  fun update(awaitedCover: AwaitedCover)

  fun findAll(): List<AwaitedCover>

  fun delete(awaitedCover: AwaitedCover)
}
