package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.application.edition.EditEditionResult.Edited
import fr.amory.libris.bibliography.application.edition.EditEditionResult.NoSuchEdition
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import org.springframework.stereotype.Service

@Service
class EditEdition(private val editions: EditionRepository) {
  operator fun invoke(editionId: EditionId, details: EditionDetails): EditEditionResult {
    val held = editions
      .findByIds(listOf(editionId))
      .singleOrNull() ?: return NoSuchEdition
    val edited = editedOf(held, details)
    editions.update(edited)
    return Edited(edited)
  }

  private fun editedOf(held: Edition, details: EditionDetails): Edition =
    held.copy(
      kind = details.kind,
      title = details.title,
      subtitle = details.subtitle,
      contributions = details.contributions,
      seriesVolume = null,
      collection = details.collection,
      publisher = details.publisher,
      publicationYear = details.publicationYear,
      language = details.language,
      pageCount = details.pageCount,
      summary = details.summary,
    )
}
