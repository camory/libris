package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.application.catalogue.SaveEditionResult.NoSuchEdition
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class SaveEdition {
  @Suppress("UnusedParameter")
  operator fun invoke(readerId: ReaderId, editionId: EditionId): SaveEditionResult =
    NoSuchEdition
}
