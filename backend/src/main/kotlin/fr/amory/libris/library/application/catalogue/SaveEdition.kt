package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.application.edition.EditEdition
import fr.amory.libris.bibliography.application.edition.EditEditionResult
import fr.amory.libris.bibliography.application.edition.EditEditionResult.Edited
import fr.amory.libris.bibliography.application.edition.EditionDetails
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.series.SeriesRepository
import fr.amory.libris.library.application.catalogue.SaveEditionResult.NoSuchEdition
import fr.amory.libris.library.application.catalogue.SaveEditionResult.Saved
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.copy.CopyRepository
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class SaveEdition(
  private val editEdition: EditEdition,
  private val bookshelves: BookshelfRepository,
  private val copies: CopyRepository,
  private val seriesRepository: SeriesRepository) {
  operator fun invoke(readerId: ReaderId, editionId: EditionId, details: EditionDetails): SaveEditionResult =
    when (val result = editEdition(editionId, details)) {
      is Edited                       -> Saved(heldOf(readerId, result.edition))
      EditEditionResult.NoSuchEdition -> NoSuchEdition
    }

  private fun heldOf(readerId: ReaderId, edition: Edition): HeldEdition =
    HeldEdition(
      edition,
      seriesEntryOf(seriesRepository, edition),
      readersCopiesOf(bookshelves, copies, readerId, edition.id),
    )
}
