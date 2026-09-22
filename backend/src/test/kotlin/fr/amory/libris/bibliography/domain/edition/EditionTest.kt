package fr.amory.libris.bibliography.domain.edition

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test

class EditionTest {
    @Test
    fun `an edition without a title is refused`() {
        shouldThrow<IllegalArgumentException> { editionBy().copy(title = " ") }
    }

    private fun editionBy(): Edition = Edition(
        id = EditionId.new(),
        isbn = null,
        kind = MANGA,
        title = "Romance dawn",
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
