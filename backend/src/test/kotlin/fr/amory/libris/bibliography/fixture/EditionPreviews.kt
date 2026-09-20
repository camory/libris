package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.Kind.BOOK
import fr.amory.libris.bibliography.domain.lookup.EditionPreview

val A_PREVIEW = EditionPreview(
    isbn = isbnOf("9782723488525"),
    kind = BOOK,
    title = "Un ouvrage",
    subtitle = null,
    contributions = emptyList(),
    series = null,
    collection = null,
    publisher = null,
    publicationYear = null,
    language = null,
    pageCount = null,
    summary = null,
    coverUrl = null,
)
