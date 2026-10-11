package fr.amory.libris.library.application

import fr.amory.libris.library.domain.reader.Reader
import org.springframework.stereotype.Service

@Service
class ListReaders {
  operator fun invoke(): List<Reader> =
    emptyList()
}
