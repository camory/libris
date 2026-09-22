package fr.amory.libris.library.application

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesEntry

data class NewBook(
    val isbn13: String?,
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
)
