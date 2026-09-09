package fr.amory.libris.application

import fr.amory.libris.domain.DuplicateUsernameException
import fr.amory.libris.domain.Reader
import fr.amory.libris.domain.ReaderRepository
import org.springframework.stereotype.Service

@Service
class ReaderVisit(private val readers: ReaderRepository) {
    fun visit(username: String, email: String, displayName: String): Reader {
        readers.findByUsername(username)?.let { return it }
        val reader = Reader(username = username, email = email, displayName = displayName)
        return try {
            readers.insert(reader)
            reader
        } catch (duplicate: DuplicateUsernameException) {
            readers.findByUsername(username) ?: throw duplicate
        }
    }
}
