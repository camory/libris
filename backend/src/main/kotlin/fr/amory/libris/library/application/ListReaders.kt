package fr.amory.libris.library.application

import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderRepository
import org.springframework.stereotype.Service

@Service
class ListReaders(private val readers: ReaderRepository) {
  operator fun invoke(): List<Reader> =
    readers.findAll()
}
