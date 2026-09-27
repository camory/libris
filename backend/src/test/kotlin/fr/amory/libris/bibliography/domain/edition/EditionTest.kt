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

    @Test
    fun `the order ignores accents`() {
        val editions = listOf(
            edition(title = "Légendes en exil", series = SeriesEntry("Fables", 1)),
            edition(title = "Émile et les détectives"),
        )

        editions.sortedWith(BY_SERIES_AND_VOLUME).map { it.title } shouldBe
            listOf("Émile et les détectives", "Légendes en exil")
    }

    @Test
    fun `the tomes of a series are ordered as numbers`() {
        val editions = listOf(
            edition(title = "Le vrai visage d'Arlong", series = SeriesEntry("One piece", 10)),
            edition(title = "Une vérité qui blesse", series = SeriesEntry("One piece", 3)),
        )

        editions.sortedWith(BY_SERIES_AND_VOLUME).map { it.title } shouldBe
            listOf("Une vérité qui blesse", "Le vrai visage d'Arlong")
    }

    @Test
    fun `an edition of the series without a tome comes after its numbered tomes`() {
        val editions = listOf(
            edition(title = "Astérix et ses amis", series = SeriesEntry("Astérix", null)),
            edition(title = "Astérix le Gaulois", series = SeriesEntry("Astérix", 1)),
        )

        editions.sortedWith(BY_SERIES_AND_VOLUME).map { it.title } shouldBe
            listOf("Astérix le Gaulois", "Astérix et ses amis")
    }

    @Test
    fun `editions of one series and tome are ordered by title`() {
        val editions = listOf(
            edition(title = "Romance dawn", series = SeriesEntry("One piece", 1)),
            edition(title = "À l'aube d'une grande aventure", series = SeriesEntry("One piece", 1)),
        )

        editions.sortedWith(BY_SERIES_AND_VOLUME).map { it.title } shouldBe
            listOf("À l'aube d'une grande aventure", "Romance dawn")
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
