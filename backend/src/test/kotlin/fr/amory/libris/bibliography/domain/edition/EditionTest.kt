package fr.amory.libris.bibliography.domain.edition

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class EditionTest {
    @Test
    fun `an edition without a title is refused`() {
        shouldThrow<IllegalArgumentException> { edition(title = " ") }
    }

    private fun edition(title: String = "Romance dawn"): Edition = Edition(
        id = EditionId.new(),
        isbn = null,
        kind = MANGA,
        title = title,
        subtitle = null,
        contributions = Contributions.of(emptyList()),
        series = null,
        collection = null,
        publisher = null,
        publicationYear = null,
        language = null,
        pageCount = null,
        summary = null,
        coverUrl = null,
    )
}
