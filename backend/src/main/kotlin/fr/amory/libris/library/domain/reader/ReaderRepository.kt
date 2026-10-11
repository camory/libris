package fr.amory.libris.library.domain.reader

interface ReaderRepository {
  fun insert(reader: Reader)

  fun findByUsername(username: String): Reader?

  fun findById(id: ReaderId): Reader?

  fun findByIds(ids: List<ReaderId>): List<Reader>

  fun findAll(): List<Reader>
}
