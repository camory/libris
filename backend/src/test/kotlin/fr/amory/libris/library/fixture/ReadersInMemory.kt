package fr.amory.libris.library.fixture

import fr.amory.libris.library.domain.reader.DuplicateUsernameException
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderRepository

class ReadersInMemory : ReaderRepository {
    private val readers = mutableMapOf<String, Reader>()

    val stored: List<Reader> get() = readers.values.toList()

    override fun insert(reader: Reader) {
        if (reader.username in readers) throw DuplicateUsernameException(reader.username)
        readers[reader.username] = reader
    }

    override fun findByUsername(username: String): Reader? = readers[username]

    fun clear() = readers.clear()
}
