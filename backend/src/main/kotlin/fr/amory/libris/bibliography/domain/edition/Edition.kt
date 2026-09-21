package fr.amory.libris.bibliography.domain.edition

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.SeriesEntry

data class Edition(
    val id: EditionId,
    val isbn: Isbn?,
    val kind: Kind,
    val title: String,
    val subtitle: String?,
    val contributions: List<Contribution>,
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
        require(contributions.distinctBy { it.name.lowercase() to it.role }.size == contributions.size) {
            "an edition names an author once per role"
        }
    }
}
