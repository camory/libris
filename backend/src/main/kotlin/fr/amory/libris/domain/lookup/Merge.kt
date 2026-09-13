package fr.amory.libris.domain.lookup

import fr.amory.libris.domain.Isbn13

private const val COVERS = "https://covers.openlibrary.org/b/isbn"

fun merge(isbn: Isbn13, editions: List<SourceEdition>): SourceEdition =
    editions.first().copy(isbn13 = isbn, coverUrl = "$COVERS/${isbn.digits}-L.jpg")
