package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.application.catalogue.FindCatalogueEditionResult.NoSuchEdition
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class FindCatalogueEdition {
  @Suppress("UnusedParameter")
  operator fun invoke(readerId: ReaderId, editionId: EditionId): FindCatalogueEditionResult =
    NoSuchEdition
}
