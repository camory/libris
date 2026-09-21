package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.Isbn

fun isbnOf(text: String): Isbn = checkNotNull(Isbn.of(text))
