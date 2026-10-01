package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.library.application.WelcomeReader
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.shared.infrastructure.web.RequestPrincipal
import org.springframework.stereotype.Component

@Component
class ReaderPrincipal(private val welcomeReader: WelcomeReader) : RequestPrincipal {
    override fun of(username: String, email: String, displayName: String): Reader =
        welcomeReader(username, email, displayName)
}
