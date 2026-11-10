package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.application.edition.EditEditionResult.NoSuchEdition
import fr.amory.libris.bibliography.domain.edition.EditionId
import org.springframework.stereotype.Service

@Service
class EditEdition {
  @Suppress("UnusedParameter")
  operator fun invoke(editionId: EditionId, details: EditionDetails): EditEditionResult =
    NoSuchEdition
}
