package fr.amory.libris.fixture

import fr.amory.libris.domain.DuplicateUsernameException
import fr.amory.libris.domain.Reader
import fr.amory.libris.domain.ReaderRepository

class ReadersInMemory : ReaderRepository {
    private val stored = mutableMapOf<String, Reader>()

    override fun insert(reader: Reader) {
        if (reader.username in stored) throw DuplicateUsernameException(reader.username)
        stored[reader.username] = reader
    }

    override fun findByUsername(username: String): Reader? = stored[username]

    fun clear() = stored.clear()
}
