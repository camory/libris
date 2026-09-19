package fr.amory.libris.fixture

import fr.amory.libris.domain.Kind.BOOK
import fr.amory.libris.domain.lookup.SourceEdition

val A_SOURCE_EDITION = SourceEdition(
    isbn = isbnOf("9782723488525"),
    kind = BOOK,
    title = "Un ouvrage",
    subtitle = null,
    authors = emptyList(),
    series = null,
    collection = null,
    publisher = null,
    publicationYear = null,
    language = null,
    pageCount = null,
    summary = null,
    coverUrl = null,
)
