package fr.amory.libris.bibliography.domain.edition

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesEntry
import java.text.Collator
import java.util.Locale

private val TEXT: Collator = Collator.getInstance(Locale.FRENCH).apply { strength = Collator.PRIMARY }

data class Edition(
    val id: EditionId,
    val isbn: Isbn?,
    val kind: Kind,
    val title: String,
    val subtitle: String?,
    val contributions: Contributions,
    val series: SeriesEntry?,
    val collection: String?,
    val publisher: String?,
    val publicationYear: Int?,
    val language: String?,
    val pageCount: Int?,
    val summary: String?,
    val coverUrl: String?,
) {
    init {
        require(title.isNotBlank()) { "an edition needs a title" }
    }

    companion object {
        val BY_SERIES_AND_VOLUME: Comparator<Edition> = compareBy(TEXT) { it.series?.name ?: it.title }
    }
}
