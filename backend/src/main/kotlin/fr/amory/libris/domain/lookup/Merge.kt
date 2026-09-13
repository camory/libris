package fr.amory.libris.domain.lookup

import fr.amory.libris.domain.Isbn13

private const val COVERS = "https://covers.openlibrary.org/b/isbn"

fun merge(isbn: Isbn13, editions: List<SourceEdition>): SourceEdition = SourceEdition(
    isbn13 = isbn,
    title = editions.first().title,
    subtitle = editions.firstNotNullOfOrNull { it.subtitle },
    authors = editions.firstOrNull { it.authors.isNotEmpty() }?.authors.orEmpty(),
    series = editions.firstNotNullOfOrNull { it.series },
    collection = editions.firstNotNullOfOrNull { it.collection },
    publisher = editions.firstNotNullOfOrNull { it.publisher },
    publicationYear = editions.firstNotNullOfOrNull { it.publicationYear },
    language = editions.firstNotNullOfOrNull { it.language },
    pageCount = editions.firstNotNullOfOrNull { it.pageCount },
    summary = editions.firstNotNullOfOrNull { it.summary },
    coverUrl = "$COVERS/${isbn.digits}-L.jpg",
)
