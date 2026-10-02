package fr.amory.libris.bibliography.domain.cover

import fr.amory.libris.bibliography.domain.Isbn

data class AwaitedCover(val isbn: Isbn, val chosenSource: CoverSource?)
