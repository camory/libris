package fr.amory.libris.domain

@JvmInline
value class Isbn13 private constructor(val digits: String) {
    companion object {
        private const val LENGTH = 13
        private const val ODD_WEIGHT = 3
        private const val MODULUS = 10

        fun of(text: String): Isbn13? = when {
            text.length != LENGTH -> null
            !text.all { it.isDigit() } -> null
            text.last().digitToInt() != checkDigitOf(text.dropLast(1)) -> null
            else -> Isbn13(text)
        }

        private fun checkDigitOf(body: String): Int {
            val weighted = body.mapIndexed { position, digit ->
                digit.digitToInt() * if (position % 2 == 0) 1 else ODD_WEIGHT
            }
            return (MODULUS - weighted.sum() % MODULUS) % MODULUS
        }
    }
}
