package fr.amory.libris

import dev.contracteer.verifier.junit.ContracteerServerPort
import dev.contracteer.verifier.junit.ContracteerTest
import fr.amory.libris.application.IsbnLookup
import fr.amory.libris.application.LookupResult.Found
import fr.amory.libris.application.LookupResult.SourcesUnavailable
import fr.amory.libris.application.LookupResult.UnknownIsbn
import fr.amory.libris.application.ReaderVisit
import fr.amory.libris.domain.AuthorRole.ARTIST
import fr.amory.libris.domain.AuthorRole.WRITER
import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.Reader
import fr.amory.libris.domain.lookup.Source.BNF
import fr.amory.libris.domain.lookup.Source.OPEN_LIBRARY
import fr.amory.libris.domain.lookup.SourceAuthor
import fr.amory.libris.domain.lookup.SourceEdition
import fr.amory.libris.domain.lookup.SourceSeries
import fr.amory.libris.infra.web.WebSliceTest
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

private fun isbn13Of(text: String): Isbn13 = checkNotNull(Isbn13.of(text))

private val ONE_PIECE_1 = SourceEdition(
    isbn13 = isbn13Of("9782723488525"),
    title = "Romance dawn",
    subtitle = "à l'aube d'une grande aventure",
    authors = listOf(SourceAuthor("Eiichirō Oda", WRITER), SourceAuthor("Eiichirō Oda", ARTIST)),
    series = SourceSeries("One piece", 1),
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
@MockitoBean(types = [ReaderVisit::class, IsbnLookup::class])
class ApiContractTest @Autowired constructor(
    @field:ContracteerServerPort @param:LocalServerPort val serverPort: Int,
    private val visit: ReaderVisit,
    private val lookup: IsbnLookup,
) {
    @ContracteerTest(openApiDoc = "https://raw.githubusercontent.com/camory/libris-api/v0.3.0/openapi.yaml")
    fun `the API matches the contract`() {
        given(visit.visit("contracteer", "contracteer@amory.fr", "Contracteer"))
            .willReturn(Reader(username = "contracteer", email = "contracteer@amory.fr", displayName = "Contracteer"))
        given(lookup.lookUp(isbn13Of("9782723488525"))).willReturn(Found(ONE_PIECE_1, listOf(BNF, OPEN_LIBRARY)))
        given(lookup.lookUp(isbn13Of("9782000000006"))).willReturn(UnknownIsbn)
        given(lookup.lookUp(isbn13Of("9791000000008"))).willReturn(SourcesUnavailable)
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
            )
        }
    }
}
