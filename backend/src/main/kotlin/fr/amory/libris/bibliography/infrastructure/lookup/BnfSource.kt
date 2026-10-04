package fr.amory.libris.bibliography.infrastructure.lookup

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.TRANSLATOR
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Contributions
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.BOOK
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverFetch
import fr.amory.libris.bibliography.domain.cover.CoverSource
import fr.amory.libris.bibliography.domain.lookup.CoverCandidate
import fr.amory.libris.bibliography.domain.lookup.EditionLookup
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.domain.lookup.EditionSource
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.Failed
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.Known
import fr.amory.libris.bibliography.domain.lookup.EditionSourceAnswer.NothingKnown
import org.springframework.web.client.RestClientException
import org.springframework.web.client.toEntity
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.StringReader
import java.time.Duration
import javax.xml.parsers.DocumentBuilderFactory

private const val MARCXCHANGE = "info:lc/xmlns/marcxchange-v2"
private val AUTHOR_TAGS = setOf("700", "701", "702")
private val ROLES = mapOf("070" to WRITER, "440" to ARTIST, "730" to TRANSLATOR)
private val LANGUAGES = mapOf("fre" to "fr")
private val MANGA_LANGUAGES = setOf("jpn", "kor", "chi")
private const val COMIC_STRIP = 't'
private const val FORM_AT = 4
private const val FORM_LENGTH = 4
private val YEAR = Regex("\\d{4}")
private val PAGES = Regex("(\\d+)\\s*p\\b")
private val TOME = Regex("tome (\\d+)")
private const val YEAR_AT = 9
private const val YEAR_LENGTH = 4
private const val PUBLISHER_INDICATOR = "0"
private const val ARK = "ark:/"
private const val PUBLIC_COVERS = "https://catalogue.bnf.fr/couverture"

internal fun kindOf(codedData: String?, translatedFrom: String?): Kind =
  when {
    COMIC_STRIP !in codedData.orEmpty().drop(FORM_AT).take(FORM_LENGTH) -> BOOK
    translatedFrom in MANGA_LANGUAGES                                   -> MANGA
    else                                                                -> BD
  }

internal fun contributionRoleOf(functionCode: String?): ContributionRole =
  ROLES[functionCode] ?: WRITER

internal fun publicationYearOf(dateOfPublication: String?, publication: String?): Int? =
  dateOfPublication?.drop(YEAR_AT)?.take(YEAR_LENGTH)?.takeIf { YEAR.matches(it) }?.toInt()
    ?: YEAR.find(publication.orEmpty())?.value?.toIntOrNull()

internal fun tomeOf(partNumber: String?): Int? =
  TOME.find(partNumber.orEmpty())?.groupValues?.get(1)?.toIntOrNull()

internal fun pageCountOf(extent: String?): Int? =
  PAGES.find(extent.orEmpty())?.groupValues?.get(1)?.toIntOrNull()

internal fun coverOf(controlField: String?): CoverCandidate? =
  arkOf(controlField)?.let { CoverCandidate(CoverSource.BNF, coverAddress(PUBLIC_COVERS, it)) }

private fun arkOf(controlField: String?): String? =
  controlField
    ?.indexOf(ARK)
    ?.takeIf { it >= 0 }
    ?.let { controlField.substring(it) }

private fun publicationOf(record: UnimarcRecord): UnimarcField? =
  record.field("214", PUBLISHER_INDICATOR) ?: record.field("210")

private fun nameOf(field: UnimarcField): String =
  "${field.value("b").orEmpty()} ${field.value("a").orEmpty()}".trim()

private fun coverAddress(coversUrl: String, ark: String): String =
  "$coversUrl?&appName=NE&idArk=$ark&couverture=1"

class BnfSource(baseUrl: String, private val coversUrl: String, timeout: Duration) : EditionLookup, CoverFetch {
  override val source = EditionSource.BNF
  override val coverSource = CoverSource.BNF
  private val http = sourceRestClient(baseUrl, timeout)

  override fun lookUp(isbn: Isbn): EditionSourceAnswer =
    try {
      answerFor(isbn)
    } catch (ignored: RestClientException) {
      Failed
    } catch (ignored: SAXException) {
      Failed
    }

  override fun fetch(isbn: Isbn): Cover? =
    recordIn(search(isbn))
      ?.let { arkOf(it.control("003")) }
      ?.let { pictureOf(it) }

  private fun pictureOf(ark: String): Cover? {
    val answer = http
      .get()
      .uri(coverAddress(coversUrl, ark))
      .retrieve()
      .toEntity<ByteArray>()
    return answer.body?.let { bytes -> answer.headers.contentType?.let { Cover.of(it.toString(), bytes) } }
  }

  private fun answerFor(isbn: Isbn): EditionSourceAnswer =
    recordIn(search(isbn))?.let { answerFrom(isbn, it) } ?: NothingKnown

  private fun answerFrom(isbn: Isbn, record: UnimarcRecord): EditionSourceAnswer =
    record.value("200", "a")?.let { Known(previewOf(isbn, it, record), coverOf(record.control("003"))) } ?: Failed

  private fun previewOf(isbn: Isbn, title: String, record: UnimarcRecord): EditionPreview {
    val publication = publicationOf(record)
    return EditionPreview(
      isbn = isbn,
      kind = kindOf(record.value("105", "a"), record.value("101", "c")),
      title = title,
      subtitle = record.value("200", "e"),
      contributions = contributionsOf(record),
      series = seriesOf(record),
      collection = record.value("410", "t"),
      publisher = publication?.value("c"),
      publicationYear = publicationYearOf(record.value("100", "a"), publication?.value("d")),
      language = LANGUAGES[record.value("101", "a")],
      pageCount = pageCountOf(record.value("215", "a")),
      summary = null,
    )
  }

  private fun contributionsOf(record: UnimarcRecord): Contributions =
    Contributions.of(
      record.fields(AUTHOR_TAGS).mapNotNull { field ->
        Contribution.of(nameOf(field), contributionRoleOf(field.value("4")))
      },
    )

  private fun seriesOf(record: UnimarcRecord): SeriesEntry? =
    SeriesEntry.of(record.value("461", "t"), record.value("461", "v")?.toIntOrNull())
      ?: titleStatementSeriesOf(record)

  private fun titleStatementSeriesOf(record: UnimarcRecord): SeriesEntry? =
    tomeOf(record.value("200", "h"))?.let { tome -> SeriesEntry.of(record.value("200", "a"), tome) }

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

    fun recordIn(answer: String): UnimarcRecord? {
      val factory = DocumentBuilderFactory.newInstance().apply {
        isNamespaceAware = true
        setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
      }
      val document = factory.newDocumentBuilder().parse(InputSource(StringReader(answer)))
      val record = document.getElementsByTagNameNS(MARCXCHANGE, "record").item(0) as Element?
      return record?.let { UnimarcRecord(controlsOf(it), fieldsOf(it)) }
    }

    fun controlsOf(record: Element): List<Pair<String, String>> =
      record.getElementsByTagNameNS(MARCXCHANGE, "controlfield").let { controls ->
        (0 until controls.length).map { index ->
          (controls.item(index) as Element).let { it.getAttribute("tag") to it.textContent }
        }
      }

    fun fieldsOf(record: Element): List<UnimarcField> =
      record.getElementsByTagNameNS(MARCXCHANGE, "datafield").let { fields ->
        (0 until fields.length).map { index -> fieldOf(fields.item(index) as Element) }
      }

    fun fieldOf(field: Element): UnimarcField =
      field.getElementsByTagNameNS(MARCXCHANGE, "subfield").let { subfields ->
        UnimarcField(
          tag = field.getAttribute("tag"),
          indicator2 = field.getAttribute("ind2"),
          subfields = (0 until subfields.length).map { index ->
            (subfields.item(index) as Element).let { it.getAttribute("code") to it.textContent }
          },
        )
      }
  }
}

private class UnimarcRecord(
  private val controls: List<Pair<String, String>>,
  private val fields: List<UnimarcField>) {
  fun control(tag: String): String? =
    controls.firstOrNull { it.first == tag }?.second

  fun value(tag: String, code: String): String? =
    field(tag)?.value(code)

  fun field(tag: String): UnimarcField? =
    fields.firstOrNull { it.tag == tag }

  fun field(tag: String, indicator2: String): UnimarcField? =
    fields.firstOrNull { it.tag == tag && it.indicator2 == indicator2 }

  fun fields(tags: Set<String>): List<UnimarcField> =
    fields.filter { it.tag in tags }
}

private class UnimarcField(
  val tag: String,
  val indicator2: String,
  private val subfields: List<Pair<String, String>>) {
  fun value(code: String): String? =
    subfields.firstOrNull { it.first == code }?.second
}
