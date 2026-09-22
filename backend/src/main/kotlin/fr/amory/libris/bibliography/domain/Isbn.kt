package fr.amory.libris.bibliography.domain

@JvmInline
value class Isbn private constructor(val digits: String) {
    val isbn10: String?
        get() = digits.takeIf { it.startsWith(TEN_PREFIX) }
            ?.substring(TEN_PREFIX.length, LENGTH - 1)
            ?.let { it + tenCheckDigitOf(it) }

    companion object {
        private const val SEPARATORS = "- "
        private val THIRTEEN_DIGITS = Regex("97[89][0-9]{10}")
        private const val LENGTH = 13
        private const val ODD_WEIGHT = 3
        private const val MODULUS = 10
        private const val TEN_PREFIX = "978"
        private const val TEN_LENGTH = 10
        private const val TEN_MODULUS = 11
        private const val TEN_FIRST_WEIGHT = 10

        fun of(text: String): Isbn? {
            val characters = text.filterNot { it in SEPARATORS }
            return if (characters.length == TEN_LENGTH) {
                ofTen(characters)
            } else {
                ofThirteen(characters)
            }
        }

        fun ofThirteen(text: String): Isbn? = when {
            !THIRTEEN_DIGITS.matches(text) -> null
            text.last().digitToInt() != checkDigitOf(text.dropLast(1)) -> null
            else -> Isbn(text)
        }

        private fun ofTen(text: String): Isbn? {
            val nine = text.dropLast(1)
            if (!nine.all { it in '0'..'9' } || text.last().uppercaseChar() != tenCheckDigitOf(nine)) {
                return null
            }
            val twelve = TEN_PREFIX + nine
            return ofThirteen(twelve + checkDigitOf(twelve))
        }

        private fun checkDigitOf(body: String): Int {
            val weighted = body.mapIndexed { position, digit ->
                digit.digitToInt() * if (position % 2 == 0) 1 else ODD_WEIGHT
            }
            return (MODULUS - weighted.sum() % MODULUS) % MODULUS
        }

        private fun tenCheckDigitOf(body: String): Char {
            val weighted = body.mapIndexed { position, digit ->
                digit.digitToInt() * (TEN_FIRST_WEIGHT - position)
            }
            val check = (TEN_MODULUS - weighted.sum() % TEN_MODULUS) % TEN_MODULUS
            return if (check == TEN_MODULUS - 1) 'X' else '0' + check
        }
    }
}
