package fr.amory.libris.fixture

import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.lookup.SourceAuthor
import fr.amory.libris.domain.lookup.SourceEdition
import fr.amory.libris.domain.lookup.SourceSeries

fun sourceEdition(
    isbn13: Isbn13 = isbn13Of("9782723488525"),
    title: String = "Un ouvrage",
    subtitle: String? = null,
    authors: List<SourceAuthor> = emptyList(),
    series: SourceSeries? = null,
    collection: String? = null,
    publisher: String? = null,
    publicationYear: Int? = null,
    language: String? = null,
    pageCount: Int? = null,
    summary: String? = null,
    coverUrl: String? = null,
): SourceEdition = SourceEdition(
    isbn13 = isbn13,
    title = title,
    subtitle = subtitle,
    authors = authors,
    series = series,
    collection = collection,
    publisher = publisher,
    publicationYear = publicationYear,
    language = language,
    pageCount = pageCount,
    summary = summary,
    coverUrl = coverUrl,
)
