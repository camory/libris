package fr.amory.libris.infra.lookup

import fr.amory.libris.domain.Isbn13
import fr.amory.libris.domain.lookup.IsbnSource
import fr.amory.libris.domain.lookup.Source.BNF
import fr.amory.libris.domain.lookup.SourceAnswer
import fr.amory.libris.domain.lookup.SourceAnswer.NothingKnown
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.net.http.HttpClient.Redirect.NORMAL
import java.time.Duration

class BnfSource(baseUrl: String, timeout: Duration) : IsbnSource {
    override val source = BNF

    private val http = RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(requestFactory(timeout))
        .build()

    override fun lookUp(isbn: Isbn13): SourceAnswer {
        search(isbn)
        return NothingKnown
    }

    private fun search(isbn: Isbn13): String? = http.get()
        .uri { uri ->
            uri.queryParam("version", "1.2")
                .queryParam("operation", "searchRetrieve")
                .queryParam("recordSchema", "unimarcxchange")
                .queryParam("maximumRecords", "1")
                .queryParam("query", """bib.isbn all "${isbn.digits}"""")
                .build()
        }
        .retrieve()
        .body(String::class.java)

    private companion object {
        fun requestFactory(timeout: Duration): JdkClientHttpRequestFactory {
            val client = HttpClient.newBuilder()
                .followRedirects(NORMAL)
                .connectTimeout(timeout)
                .build()
            return JdkClientHttpRequestFactory(client).apply { setReadTimeout(timeout) }
        }
    }
}
