package fr.amory.libris.fixture

import fr.amory.libris.domain.Isbn13

fun isbn13Of(text: String): Isbn13 = checkNotNull(Isbn13.of(text))
