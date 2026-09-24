package fr.amory.libris

import dev.contracteer.verifier.junit.ContracteerServerPort
import dev.contracteer.verifier.junit.ContracteerTest
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.Found
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.SourcesUnavailable
import fr.amory.libris.bibliography.application.lookup.EditionLookupResult.UnknownIsbn
import fr.amory.libris.bibliography.application.lookup.LookupEditionByIsbn
import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.fixture.isbnOf
import fr.amory.libris.fixture.WebSliceTest
import fr.amory.libris.library.application.WelcomeReader
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

@WebSliceTest
@Import(ApiContractTest.FixedReaderHeaders::class)
@MockitoBean(types = [WelcomeReader::class, LookupEditionByIsbn::class])
class ApiContractTest @Autowired constructor(
    @field:ContracteerServerPort @param:LocalServerPort val serverPort: Int,
    private val welcomeReader: WelcomeReader,
    private val lookupEditionByIsbn: LookupEditionByIsbn,
) {
    @ContracteerTest(openApiDoc = "https://raw.githubusercontent.com/camory/libris-api/v0.6.0/openapi.yaml")
    fun `the API matches the contract`() {
        given(welcomeReader("contracteer", "contracteer@amory.fr", "Contracteer"))
            .willReturn(readerNamed("contracteer", "Contracteer"))
        given(lookupEditionByIsbn(isbnOf("9782723488525"))).willReturn(Found(ONE_PIECE_1))
        given(lookupEditionByIsbn(isbnOf("9782000000006"))).willReturn(UnknownIsbn)
        given(lookupEditionByIsbn(isbnOf("9791000000008"))).willReturn(SourcesUnavailable)
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
