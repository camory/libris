package fr.amory.libris.domain

@JvmInline
value class Isbn13 private constructor(val digits: String) {
    companion object {
        fun of(text: String): Isbn13? = Isbn13(text)
    }
}
