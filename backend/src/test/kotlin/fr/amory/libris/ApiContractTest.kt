package fr.amory.libris

import dev.contracteer.verifier.junit.ContracteerServerPort
import dev.contracteer.verifier.junit.ContracteerTest
import fr.amory.libris.bibliography.application.cover.FindCover
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Found
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Held
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.SourcesUnavailable
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.UnknownIsbn
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.cover.CoverSource.INVENTAIRE
import fr.amory.libris.bibliography.domain.cover.CoverSource.OPEN_LIBRARY
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.lookup.CoverCandidate
import fr.amory.libris.bibliography.domain.lookup.CoverCandidates
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.fixture.coverOf
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.bibliography.fixture.recordedBytes
import fr.amory.libris.fixture.WebSliceTest
import fr.amory.libris.library.application.AddBookResult.Added
import fr.amory.libris.library.application.AddBookResult.NotAnOwner
import fr.amory.libris.library.application.AddBookToBookshelf
import fr.amory.libris.library.application.FindDefaultBookshelf
import fr.amory.libris.library.application.NewBook
import fr.amory.libris.library.application.WelcomeReader
import fr.amory.libris.library.application.catalogue.BrowseCatalogue
import fr.amory.libris.library.application.catalogue.CataloguePage
import fr.amory.libris.library.application.catalogue.HeldEdition
import fr.amory.libris.library.application.lookup.CopyOnBookshelf
import fr.amory.libris.library.application.lookup.IsbnLookup
import fr.amory.libris.library.application.lookup.LookupIsbnForReader
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.fixture.bookshelfOwnedBy
import fr.amory.libris.library.fixture.readerNamed
import jakarta.servlet.Filter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
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

@WebSliceTest
@Import(ApiContractTest.FixedReaderHeaders::class)
@MockitoBean(
  types = [
    WelcomeReader::class,
    LookupIsbnForReader::class,
    FindDefaultBookshelf::class,
    AddBookToBookshelf::class,
    BrowseCatalogue::class,
    FindCover::class,
  ],
)
@Suppress("LongParameterList")
class ApiContractTest @Autowired constructor(
  @field:ContracteerServerPort @param:LocalServerPort val serverPort: Int,
  private val welcomeReader: WelcomeReader,
  private val lookupIsbnForReader: LookupIsbnForReader,
  private val findDefaultBookshelf: FindDefaultBookshelf,
  private val addBookToBookshelf: AddBookToBookshelf,
  private val browseCatalogue: BrowseCatalogue,
  private val findCover: FindCover) {
  @ContracteerTest(openApiDoc = "https://raw.githubusercontent.com/camory/libris-api/v0.9.0/openapi.yaml")
  fun `the API matches the contract`() {
    contracteerIsWelcomed()
    isbnLookupsAnswer()
    addingABookAnswers()
    catalogueAnswers()
    coversAnswer()
  }

  private fun contracteerIsWelcomed() {
    given(welcomeReader("contracteer", "contracteer@amory.fr", "Contracteer")).willReturn(CONTRACTEER)
    given(findDefaultBookshelf(CONTRACTEER)).willReturn(CONTRACTEER_BOOKSHELF)
  }

  private fun isbnLookupsAnswer() {
    given(lookupIsbnForReader(CONTRACTEER.id, isbnOf("9782723488525")))
      .willReturn(noCopy(Found(ASTERIX_1, ASTERIX_1_COVERS)))
    given(lookupIsbnForReader(CONTRACTEER.id, isbnOf("9782000000013"))).willReturn(noCopy(UnknownIsbn))
    given(lookupIsbnForReader(CONTRACTEER.id, isbnOf("9791000000008"))).willReturn(noCopy(SourcesUnavailable))
    given(lookupIsbnForReader(CONTRACTEER.id, isbnOf("9782723489898")))
      .willReturn(IsbnLookup(Held(EditionId.new(), ASTERIX_1), listOf(COPY_OF_ASTERIX_1)))
  }

  private fun addingABookAnswers() {
    given(addBookToBookshelf(CONTRACTEER.id, CONTRACTEER_BOOKSHELF.id, NEW_ONE_PIECE_1))
      .willReturn(Added(Copy(CopyId.new(), EditionId.new(), CONTRACTEER_BOOKSHELF.id), CONTRACTEER_BOOKSHELF))
    given(addBookToBookshelf(CONTRACTEER.id, bookshelfId("9e8d7c6b-5a4f-4e3d-8c2b-1a0f9e8d7c6b"), NEW_ONE_PIECE_1))
      .willReturn(NotAnOwner)
  }

  private fun catalogueAnswers() {
    given(browseCatalogue(ReaderId(eq(CONTRACTEER.id.value) ?: CONTRACTEER.id.value), any()))
      .willReturn(CataloguePage(listOf(ASTERIX_1_HELD), null))
  }

  private fun coversAnswer() {
    given(findCover(CoverName(any() ?: NO_COVER)))
      .willReturn(coverOf("image/jpeg", recordedBytes("covers/tall.jpg")))
    given(findCover(CoverName(eq(NO_COVER) ?: NO_COVER))).willReturn(null)
  }

  private fun noCopy(answer: EditionLookupResult) =
    IsbnLookup(answer, emptyList())

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

private val CONTRACTEER = readerNamed(
  "contracteer",
  "Contracteer",
  defaultBookshelfId = bookshelfId("0b1e2d3c-4f5a-4b6c-8d7e-9f0a1b2c3d4e"),
)

private val CONTRACTEER_BOOKSHELF = bookshelfOwnedBy(CONTRACTEER)

private val ASTERIX_1 = EditionPreview(
  isbn = isbnOf("9782012101333"),
  kind = BD,
  title = "Astérix le Gaulois",
  subtitle = "une aventure d'Astérix",
  contributions = Contributions.of(listOf(Contribution("René Goscinny", WRITER))),
  series = SeriesEntry("Astérix", 1),
  collection = "Les aventures d'Astérix",
  publisher = "Hachette",
  publicationYear = 1961,
  language = "fr",
  pageCount = 48,
  summary = "Un village d'irréductibles Gaulois résiste encore à l'envahisseur.",
)

private val ASTERIX_1_COVERS = CoverCandidates.of(
  listOf(CoverCandidate(OPEN_LIBRARY, "https://covers.example.org/asterix-1.jpg")),
)

private val COPY_OF_ASTERIX_1 = CopyOnBookshelf(
  CopyId.new(),
  BookshelfId(UUID.randomUUID()),
  "Grenier",
)

private val NEW_ONE_PIECE_1 = NewBook(
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
  coverSource = INVENTAIRE,
)

private val ASTERIX_1_HELD = HeldEdition(
  edition = Edition(
    id = EditionId.new(),
    isbn = ASTERIX_1.isbn,
    kind = ASTERIX_1.kind,
    title = ASTERIX_1.title,
    subtitle = ASTERIX_1.subtitle,
    contributions = ASTERIX_1.contributions,
    series = ASTERIX_1.series,
    collection = ASTERIX_1.collection,
    publisher = ASTERIX_1.publisher,
    publicationYear = ASTERIX_1.publicationYear,
    language = ASTERIX_1.language,
    pageCount = ASTERIX_1.pageCount,
    summary = ASTERIX_1.summary,
    coverName = CoverName("9c56cc51b374c3ba189210d5b6d4bf57790d351c96c47c02190ecf1e430635ab"),
  ),
  copies = listOf(COPY_OF_ASTERIX_1),
)

private val NO_COVER = "0".repeat(64)

private fun bookshelfId(id: String) =
  BookshelfId(UUID.fromString(id))
