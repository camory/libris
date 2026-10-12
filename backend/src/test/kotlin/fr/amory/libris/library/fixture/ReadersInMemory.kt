package fr.amory.libris.library.fixture

import fr.amory.libris.library.domain.reader.DuplicateUsernameException
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.domain.reader.ReaderRepository

class ReadersInMemory : ReaderRepository {
  private val readers = mutableMapOf<ReaderId, Reader>()

  val stored: List<Reader> get() = readers.values.toList()

  override fun insert(reader: Reader) {
    if (readers.values.any { it.username == reader.username }) throw DuplicateUsernameException(reader.username)
    readers[reader.id] = reader
  }

  override fun update(reader: Reader) {
    readers[reader.id] = reader
  }

  override fun findByUsername(username: String): Reader? =
    readers.values.find { it.username == username }

  override fun findById(id: ReaderId): Reader? =
    readers[id]

  override fun findByIds(ids: List<ReaderId>): List<Reader> =
    readers.values.filter { it.id in ids }

  override fun findAll(): List<Reader> =
    stored

  fun clear() =
    readers.clear()
}
