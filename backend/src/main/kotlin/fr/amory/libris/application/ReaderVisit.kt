package fr.amory.libris.application

import fr.amory.libris.domain.Reader
import fr.amory.libris.domain.ReaderRepository
import org.springframework.stereotype.Service

@Service
class ReaderVisit(private val readers: ReaderRepository) {
    fun visit(username: String, email: String, displayName: String): Reader {
        val reader = Reader(username = username, email = email, displayName = displayName)
        readers.insert(reader)
        return reader
    }
}
