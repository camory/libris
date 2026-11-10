package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.application.edition.EditEditionResult.Edited
import fr.amory.libris.bibliography.application.edition.EditEditionResult.NoSuchEdition
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.edition.EditionRepository
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.bibliography.domain.series.SeriesId
import fr.amory.libris.bibliography.domain.series.SeriesRepository
import fr.amory.libris.bibliography.domain.series.SeriesVolume
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations

@Service
class EditEdition(
  private val editions: EditionRepository,
  private val seriesRepository: SeriesRepository,
  private val transactions: TransactionOperations) {
  operator fun invoke(editionId: EditionId, details: EditionDetails): EditEditionResult {
    val held = editions
      .findByIds(listOf(editionId))
      .singleOrNull() ?: return NoSuchEdition
    return Edited(written(held, details))
  }

  private fun written(held: Edition, details: EditionDetails): Edition =
    checkNotNull(
      transactions.execute {
        val edited = editedOf(held, details, details.series?.let { seriesVolumeOf(it) })
        editions.update(edited)
        edited
      },
    )

  private fun seriesVolumeOf(seriesEntry: SeriesEntry): SeriesVolume =
    SeriesVolume(
      seriesRepository.findOrInsert(Series(SeriesId.new(), seriesEntry.name)).id,
      seriesEntry.volumeNumber,
    )

  private fun editedOf(held: Edition, details: EditionDetails, seriesVolume: SeriesVolume?): Edition =
    held.copy(
      kind = details.kind,
      title = details.title,
      subtitle = details.subtitle,
      contributions = details.contributions,
      seriesVolume = seriesVolume,
      collection = details.collection,
      publisher = details.publisher,
      publicationYear = details.publicationYear,
      language = details.language,
      pageCount = details.pageCount,
      summary = details.summary,
    )
}
