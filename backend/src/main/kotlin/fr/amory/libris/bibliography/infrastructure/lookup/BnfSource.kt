package fr.amory.libris.bibliography.infrastructure.lookup

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverFetch
import fr.amory.libris.bibliography.domain.cover.CoverSource
import fr.amory.libris.bibliography.domain.lookup.CoverCandidate
import fr.amory.libris.bibliography.domain.lookup.EditionLookup
import fr.amory.libris.bibliography.domain.lookup.EditionSource
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.Failed
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.Known
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.NothingKnown
import org.springframework.web.client.RestClientException
import org.xml.sax.SAXException
import java.time.Duration

private const val PUBLIC_COVERS = "https://catalogue.bnf.fr/couverture"

class BnfSource(baseUrl: String, private val coversUrl: String, timeout: Duration) : EditionLookup, CoverFetch {
  override val source = EditionSource.BNF
  override val coverSource = CoverSource.BNF
  private val http = sourceRestClient(baseUrl, timeout)

  override fun answerFor(isbn: Isbn): EditionSourceAnswer =
    try {
      recordFor(isbn)?.let { read(isbn, it) } ?: NothingKnown
    } catch (ignored: RestClientException) {
      Failed
    } catch (ignored: SAXException) {
      Failed
    }

  override fun coverFor(isbn: Isbn): Cover? =
    nullOnFailure {
      recordFor(isbn)?.ark?.let { http.pictureAt(coverAddress(coversUrl, it)) }
    }

  private fun read(isbn: Isbn, record: UnimarcRecord): EditionSourceAnswer =
    record.previewFor(isbn)?.let { Known(it, record.ark?.let(::candidateOf)) } ?: Failed

  private fun recordFor(isbn: Isbn): UnimarcRecord? =
    UnimarcRecord.parse(search(isbn))

  private fun search(isbn: Isbn): String =
    http
      .get()
      .uri { uri ->
        uri
          .queryParam("version", "1.2")
          .queryParam("operation", "searchRetrieve")
          .queryParam("recordSchema", "unimarcxchange")
          .queryParam("maximumRecords", "1")
          .queryParam("query", queryFor(isbn))
          .build()
      }
      .retrieve()
      .body(String::class.java)
      .orEmpty()

  private companion object {
    fun queryFor(isbn: Isbn): String =
      listOfNotNull(isbn.digits, isbn.isbn10).joinToString(" or ") { """bib.isbn all "$it"""" }

    fun candidateOf(ark: String): CoverCandidate =
      CoverCandidate(CoverSource.BNF, coverAddress(PUBLIC_COVERS, ark))

    fun coverAddress(coversUrl: String, ark: String): String =
      "$coversUrl?&appName=NE&idArk=$ark&couverture=1"
  }
}
