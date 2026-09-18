package fr.amory.libris.domain.lookup

fun merge(editions: List<SourceEdition>): SourceEdition = SourceEdition(
    isbn = editions.first().isbn,
    kind = editions.firstNotNullOfOrNull { it.kind },
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
    coverUrl = editions.firstNotNullOfOrNull { it.coverUrl },
)
