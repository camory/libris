package fr.amory.libris.application

import fr.amory.libris.domain.DuplicateUsernameException
import fr.amory.libris.domain.Reader
import fr.amory.libris.domain.ReaderRepository
import org.springframework.stereotype.Service

@Service
class ReaderVisit(
    private val readers: ReaderRepository,
    private val firstVisit: FirstVisit,
) {
    fun visit(username: String, email: String, displayName: String): Reader {
        readers.findByUsername(username)?.let { return it }
        return try {
            firstVisit.welcome(username, email, displayName)
        } catch (duplicate: DuplicateUsernameException) {
            readers.findByUsername(username) ?: throw duplicate
        }
    }
}
