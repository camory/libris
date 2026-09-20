package fr.amory.libris.library.fixture

import fr.amory.libris.library.domain.reader.DuplicateUsernameException
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderRepository

class ReadersInMemory : ReaderRepository {
    private val stored = mutableMapOf<String, Reader>()

    override fun insert(reader: Reader) {
        if (reader.username in stored) throw DuplicateUsernameException(reader.username)
        stored[reader.username] = reader
    }

    override fun findByUsername(username: String): Reader? = stored[username]

    fun clear() = stored.clear()
}
