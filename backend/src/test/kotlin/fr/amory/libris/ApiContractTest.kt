package fr.amory.libris

import dev.contracteer.verifier.junit.ContracteerServerPort
import dev.contracteer.verifier.junit.ContracteerTest
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Found
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Held
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.SourcesUnavailable
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.UnknownIsbn
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.fixture.WebSliceTest
import fr.amory.libris.library.application.AddBookResult.Added
import fr.amory.libris.library.application.AddBookResult.NoSuchBookshelf
import fr.amory.libris.library.application.AddBookToBookshelf
import fr.amory.libris.library.application.FindDefaultBookshelf
import fr.amory.libris.library.application.NewBook
import fr.amory.libris.library.application.WelcomeReader
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.application.lookup.IsbnLookup
import fr.amory.libris.library.application.lookup.LookupIsbnForReader
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import jakarta.servlet.Filter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.Ordered
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID

private val ONE_PIECE_1 = EditionPreview(
    isbn = isbnOf("9782723488525"),
    kind = MANGA,
    title = "Romance dawn",
    subtitle = "à l'aube d'une grande aventure",
    contributions = Contributions.of(
        listOf(
            Contribution("Eiichirō Oda", WRITER),
            Contribution("Eiichirō Oda", ARTIST),
        ),
    ),
    series = SeriesEntry("One piece", 1),
    collection = "Shonen manga",
    publisher = "Glénat",
    publicationYear = 2013,
    language = "fr",
    pageCount = 203,
    summary = null,
    coverUrl = "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
)

private val ONE_PIECE_2 = ONE_PIECE_1.copy(
    isbn = isbnOf("9782723489898"),
    title = "Aux prises avec Baggy et ses hommes",
    subtitle = null,
    series = SeriesEntry("One piece", 2),
    pageCount = 208,
    coverUrl = "https://covers.openlibrary.org/b/isbn/9782723489898-L.jpg",
)

private val COPIES_OF_ONE_PIECE_2 = listOf(
    copyOn("6f1d2c3b-4a59-4e6f-8b70-1c2d3e4f5a61", "0b1e2d3c-4f5a-4b6c-8d7e-9f0a1b2c3d4e", "Bibliothèque de Léa"),
    copyOn("7a2e3d4c-5b6a-4f70-9c81-2d3e4f5a6b72", "1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f", "Salon"),
)

private fun copyOn(copyId: String, bookshelfId: String, bookshelfName: String) = CopyOnBookshelf(
    CopyId(UUID.fromString(copyId)),
    bookshelfId(bookshelfId),
    bookshelfName,
)

private val NEW_ONE_PIECE_1 = NewBook(
    isbn = ONE_PIECE_1.isbn,
    kind = ONE_PIECE_1.kind,
    title = ONE_PIECE_1.title,
    subtitle = ONE_PIECE_1.subtitle,
    contributions = ONE_PIECE_1.contributions,
    series = ONE_PIECE_1.series,
    collection = ONE_PIECE_1.collection,
    publisher = ONE_PIECE_1.publisher,
    publicationYear = ONE_PIECE_1.publicationYear,
    language = ONE_PIECE_1.language,
    pageCount = ONE_PIECE_1.pageCount,
    summary = ONE_PIECE_1.summary,
    coverUrl = ONE_PIECE_1.coverUrl,
)

private fun bookshelfId(id: String) = BookshelfId(UUID.fromString(id))

private fun noCopy(answer: EditionLookupResult) = IsbnLookup(answer, emptyList())

@WebSliceTest
@Import(ApiContractTest.FixedReaderHeaders::class)
@MockitoBean(
    types = [
        WelcomeReader::class,
        LookupIsbnForReader::class,
        FindDefaultBookshelf::class,
        AddBookToBookshelf::class,
    ],
)
class ApiContractTest @Autowired constructor(
    @field:ContracteerServerPort @param:LocalServerPort val serverPort: Int,
    private val welcomeReader: WelcomeReader,
    private val lookupIsbnForReader: LookupIsbnForReader,
    private val findDefaultBookshelf: FindDefaultBookshelf,
    private val addBookToBookshelf: AddBookToBookshelf,
) {
    @ContracteerTest(openApiDoc = "https://raw.githubusercontent.com/camory/libris-api/v0.6.1/openapi.yaml")
    fun `the API matches the contract`() {
        val contracteer = readerNamed(
            "contracteer",
            "Contracteer",
            defaultBookshelfId = bookshelfId("0b1e2d3c-4f5a-4b6c-8d7e-9f0a1b2c3d4e"),
        )
        val bookshelf = bookshelfOwnedBy(contracteer)
        given(welcomeReader("contracteer", "contracteer@amory.fr", "Contracteer")).willReturn(contracteer)
        given(findDefaultBookshelf(contracteer)).willReturn(bookshelf)
        given(lookupIsbnForReader(contracteer.id, isbnOf("9782723488525"))).willReturn(noCopy(Found(ONE_PIECE_1)))
        given(lookupIsbnForReader(contracteer.id, isbnOf("9782000000006"))).willReturn(noCopy(UnknownIsbn))
        given(lookupIsbnForReader(contracteer.id, isbnOf("9791000000008"))).willReturn(noCopy(SourcesUnavailable))
        given(lookupIsbnForReader(contracteer.id, isbnOf("9782723489898")))
            .willReturn(IsbnLookup(Held(EditionId.new(), ONE_PIECE_2), COPIES_OF_ONE_PIECE_2))
        given(addBookToBookshelf(contracteer.id, bookshelf.id, NEW_ONE_PIECE_1))
            .willReturn(Added(Copy(CopyId.new(), EditionId.new(), bookshelf.id), bookshelf))
        given(addBookToBookshelf(contracteer.id, bookshelfId("9e8d7c6b-5a4f-4e3d-8c2b-1a0f9e8d7c6b"), NEW_ONE_PIECE_1))
            .willReturn(NoSuchBookshelf)
    }

    @TestConfiguration
    class FixedReaderHeaders {
        @Bean
        fun fixedReaderFilter(): FilterRegistrationBean<Filter> {
            val registration = FilterRegistrationBean(
                Filter { request, response, chain ->
                    chain.doFilter(FixedReaderRequest(request as HttpServletRequest), response)
                },
            )
            registration.order = Ordered.HIGHEST_PRECEDENCE
            return registration
        }

        private class FixedReaderRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
            override fun getHeader(name: String): String? =
                FIXED_READER[name.lowercase()] ?: super.getHeader(name)
        }

        private companion object {
            val FIXED_READER = mapOf(
                "remote-user" to "contracteer",
                "remote-name" to "Contracteer",
                "remote-email" to "contracteer@amory.fr",
                "remote-groups" to "family",
                "x-requested-with" to "XMLHttpRequest",
            )
        }
    }
}
