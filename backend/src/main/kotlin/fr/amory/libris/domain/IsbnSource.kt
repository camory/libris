package fr.amory.libris.domain

interface IsbnSource {
    val source: Source

    fun lookUp(isbn: Isbn13): SourceAnswer
}
