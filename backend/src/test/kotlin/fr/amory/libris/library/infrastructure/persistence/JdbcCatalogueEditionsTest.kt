package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.infrastructure.persistence.JdbcEditionRepository
import fr.amory.libris.fixture.JdbcSliceTest
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.copy.EditionIdPage
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

private const val PAGE_SIZE = 50

@JdbcSliceTest
@Import(
    JdbcCatalogueEditions::class,
    JdbcCopyRepository::class,
    JdbcEditionRepository::class,
    JdbcBookshelfRepository::class,
    JdbcReaderRepository::class,
)
class JdbcCatalogueEditionsTest @Autowired constructor(
    private val catalogueEditions: JdbcCatalogueEditions,
    private val copies: JdbcCopyRepository,
    private val editions: JdbcEditionRepository,
    private val bookshelves: JdbcBookshelfRepository,
    private val readers: JdbcReaderRepository,
) {
    private val lea = readerNamed("lea", "Léa")
    private val tom = readerNamed("tom", "Tom")
    private val leasBookshelf = bookshelfOwnedBy(lea)
    private val tomsBookshelf = bookshelfOwnedBy(tom)

    @BeforeEach
    fun insertLeaAndTom() {
        listOf(lea, tom).forEach(readers::insert)
        listOf(leasBookshelf, tomsBookshelf).forEach(bookshelves::insert)
    }

    @Test
    fun `the page holds the reader's editions alone`() {
        // Given
        val leas = heldOn(leasBookshelf, edition("Les cigares du pharaon", "Tintin", 1))
        heldOn(tomsBookshelf, edition("Pierre et le loup"))

        // When
        val page = catalogueEditions.findPage(lea.id, null, PAGE_SIZE)

        // Then
        page shouldBe EditionIdPage(listOf(leas.id), null)
    }

    private fun edition(
        title: String,
        seriesName: String? = null,
        volumeNumber: Int? = null,
        id: EditionId = EditionId.new(),
    ): Edition = Edition(
        id = id,
        isbn = null,
        kind = BD,
        title = title,
        subtitle = null,
        contributions = Contributions.of(emptyList()),
        series = seriesName?.let { SeriesEntry(it, volumeNumber) },
        collection = null,
        publisher = null,
        publicationYear = null,
        language = null,
        pageCount = null,
        summary = null,
        coverUrl = null,
    ).also(editions::insert)

    private fun heldOn(bookshelf: Bookshelf, edition: Edition): Edition {
        copies.insert(Copy(CopyId.new(), edition.id, bookshelf.id))
        return edition
    }
}
