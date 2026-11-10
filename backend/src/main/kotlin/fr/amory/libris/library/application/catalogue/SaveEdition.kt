package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.application.edition.EditEdition
import fr.amory.libris.bibliography.application.edition.EditEditionResult
import fr.amory.libris.bibliography.application.edition.EditEditionResult.Edited
import fr.amory.libris.bibliography.application.edition.EditionDetails
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.library.application.catalogue.SaveEditionResult.NoSuchEdition
import fr.amory.libris.library.application.catalogue.SaveEditionResult.Saved
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class SaveEdition(private val editEdition: EditEdition) {
  @Suppress("UnusedParameter")
  operator fun invoke(readerId: ReaderId, editionId: EditionId, details: EditionDetails): SaveEditionResult =
    when (val result = editEdition(editionId, details)) {
      is Edited                       -> Saved(HeldEdition(result.edition, null, emptyList()))
      EditEditionResult.NoSuchEdition -> NoSuchEdition
    }
}
