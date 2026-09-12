package fr.amory.libris.domain

data class SourceEdition(
    val isbn13: Isbn13,
    val title: String,
    val subtitle: String?,
    val authors: List<SourceAuthor>,
    val series: SourceSeries?,
    val collection: String?,
    val publisher: String?,
    val publicationYear: Int?,
    val language: String?,
    val pageCount: Int?,
    val summary: String?,
    val coverUrl: String?,
)
