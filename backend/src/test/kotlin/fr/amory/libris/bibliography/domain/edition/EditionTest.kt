package fr.amory.libris.bibliography.domain.edition

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition.Companion.BY_SERIES_AND_VOLUME
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class EditionTest {
    @Test
    fun `an edition without a title is refused`() {
        shouldThrow<IllegalArgumentException> { edition(title = " ") }
    }

    @Test
    fun `editions are ordered by series name, or by title without a series`() {
        val editions = listOf(
            edition(title = "Les cigares du pharaon", series = SeriesEntry("Tintin", 1)),
            edition(title = "Pierre et le loup"),
        )

        editions.sortedWith(BY_SERIES_AND_VOLUME).map { it.title } shouldBe
            listOf("Pierre et le loup", "Les cigares du pharaon")
    }

    @Test
    fun `the order ignores case`() {
        val editions = listOf(
            edition(title = "Un ninja", series = SeriesEntry("Naruto", 1)),
            edition(title = "maus"),
        )

        editions.sortedWith(BY_SERIES_AND_VOLUME).map { it.title } shouldBe listOf("maus", "Un ninja")
    }

    private fun edition(title: String = "Romance dawn", series: SeriesEntry? = null): Edition = Edition(
        id = EditionId.new(),
        isbn = null,
        kind = MANGA,
        title = title,
        subtitle = null,
        contributions = Contributions.of(emptyList()),
        series = series,
        collection = null,
        publisher = null,
        publicationYear = null,
        language = null,
        pageCount = null,
        summary = null,
        coverUrl = null,
    )
}
