package fr.amory.libris.fixture

import fr.amory.libris.domain.Isbn

fun isbnOf(text: String): Isbn = checkNotNull(Isbn.of(text))
