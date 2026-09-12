package fr.amory.libris.infra.lookup

import fr.amory.libris.domain.IsbnSource
import fr.amory.libris.domain.Source
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class OpenLibrarySource(
    @Value("\${LIBRIS_OPEN_LIBRARY_URL:https://openlibrary.org}") baseUrl: String,
    @Value("\${LIBRIS_SOURCE_TIMEOUT:5s}") timeout: Duration,
) : IsbnSource {
    override val source = Source.OPEN_LIBRARY
}
