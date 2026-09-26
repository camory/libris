package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.bibliography.infrastructure.persistence.JdbcEditionRepository
import fr.amory.libris.fixture.JdbcSliceTest
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.catalogue.CatalogueCopy
import fr.amory.libris.library.domain.catalogue.CatalogueEdition
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import

private val ROMANCE_DAWN = Edition(
    id = EditionId.new(),
    isbn = isbnOf("9782723488525"),
    kind = MANGA,
    title = "Romance dawn",
    subtitle = "à l'aube d'une grande aventure",
    contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
    series = SeriesEntry("One piece", 1),
    collection = "Shonen manga",
    publisher = "Glénat",
    publicationYear = 2013,
    language = "fr",
    pageCount = 203,
    summary = "Le début de l'aventure.",
    coverUrl = "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
)

private val BAGGY = ROMANCE_DAWN.copy(
    id = EditionId.new(),
    isbn = isbnOf("9782723489898"),
    title = "Aux prises avec Baggy et ses hommes",
    series = SeriesEntry("One piece", 2),
)

@JdbcSliceTest
@Import(
    JdbcCatalogue::class,
    JdbcCopyRepository::class,
    JdbcEditionRepository::class,
    JdbcBookshelfRepository::class,
    JdbcReaderRepository::class,
)
class JdbcCatalogueTest @Autowired constructor(
    private val catalogue: JdbcCatalogue,
    private val copies: JdbcCopyRepository,
    private val editions: JdbcEditionRepository,
    private val bookshelves: JdbcBookshelfRepository,
    private val readers: JdbcReaderRepository,
) {
    @Test
    fun `the catalogue holds the copies on the reader's bookshelves, none of another's`() {
        // Given
        val lea = readerNamed("lea", "Léa").also { readers.insert(it) }
        val tom = readerNamed("tom", "Tom").also { readers.insert(it) }
        val leasBookshelf = bookshelfOwnedBy(lea).also { bookshelves.insert(it) }
        val tomsBookshelf = bookshelfOwnedBy(tom).also { bookshelves.insert(it) }
        editions.insert(ROMANCE_DAWN)
        editions.insert(BAGGY)
        val leasCopy = copyOf(ROMANCE_DAWN, leasBookshelf)
        copyOf(ROMANCE_DAWN, tomsBookshelf)
        copyOf(BAGGY, tomsBookshelf)

        // When
        val held = catalogue.editionsHeldBy(lea.id)

        // Then
        held shouldContainExactlyInAnyOrder listOf(
            CatalogueEdition(
                editionId = ROMANCE_DAWN.id,
                isbn = isbnOf("9782723488525"),
                kind = MANGA,
                title = "Romance dawn",
                subtitle = "à l'aube d'une grande aventure",
                contributions = Contributions.of(listOf(Contribution("Eiichirō Oda", WRITER))),
                series = SeriesEntry("One piece", 1),
                collection = "Shonen manga",
                publisher = "Glénat",
                publicationYear = 2013,
                language = "fr",
                pageCount = 203,
                summary = "Le début de l'aventure.",
                coverUrl = "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
                copies = listOf(CatalogueCopy(leasCopy.id, leasBookshelf.id, "Bibliothèque de Léa")),
            ),
        )
    }

    private fun copyOf(edition: Edition, bookshelf: Bookshelf): Copy =
        Copy(CopyId.new(), edition.id, bookshelf.id).also { copies.insert(it) }
}
