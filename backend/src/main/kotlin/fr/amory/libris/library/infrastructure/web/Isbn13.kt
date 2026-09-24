package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.domain.Isbn

private val THIRTEEN_DIGITS = Regex("97[89][0-9]{10}")

fun isbn13Of(text: String): Isbn? = if (THIRTEEN_DIGITS.matches(text)) Isbn.of(text) else null
