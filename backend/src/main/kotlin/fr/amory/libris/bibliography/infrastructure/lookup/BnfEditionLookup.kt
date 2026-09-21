package fr.amory.libris.bibliography.infrastructure.lookup

import fr.amory.libris.bibliography.domain.Contribution
import fr.amory.libris.bibliography.domain.ContributionRole
import fr.amory.libris.bibliography.domain.ContributionRole.ARTIST
import fr.amory.libris.bibliography.domain.ContributionRole.TRANSLATOR
import fr.amory.libris.bibliography.domain.ContributionRole.WRITER
import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.Kind.BD
import fr.amory.libris.bibliography.domain.Kind.BOOK
import fr.amory.libris.bibliography.domain.Kind.MANGA
import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.lookup.EditionPreview
import fr.amory.libris.bibliography.domain.lookup.ExternalEditionLookup
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Failed
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.Known
import fr.amory.libris.bibliography.domain.lookup.ExternalLookupResult.NothingKnown
import fr.amory.libris.bibliography.domain.lookup.Source.BNF
import org.springframework.web.client.RestClientException
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
private const val COVER_BEFORE = "https://catalogue.bnf.fr/couverture?&appName=NE&idArk="
private const val COVER_AFTER = "&couverture=1"

internal fun kindOf(codedData: String?, translatedFrom: String?): Kind =
    when {
        COMIC_STRIP !in codedData.orEmpty().drop(FORM_AT).take(FORM_LENGTH) -> BOOK
        translatedFrom in MANGA_LANGUAGES -> MANGA
        else -> BD
    }

internal fun contributionRoleOf(functionCode: String?): ContributionRole = ROLES[functionCode] ?: WRITER

internal fun publicationYearOf(dateOfPublication: String?, publication: String?): Int? =
    dateOfPublication?.drop(YEAR_AT)?.take(YEAR_LENGTH)?.takeIf { YEAR.matches(it) }?.toInt()
        ?: YEAR.find(publication.orEmpty())?.value?.toIntOrNull()

internal fun tomeOf(partNumber: String?): Int? =
    TOME.find(partNumber.orEmpty())?.groupValues?.get(1)?.toIntOrNull()

internal fun pageCountOf(extent: String?): Int? =
    PAGES.find(extent.orEmpty())?.groupValues?.get(1)?.toIntOrNull()

internal fun coverUrlOf(controlField: String?): String? =
    controlField?.indexOf(ARK)?.takeIf { it >= 0 }
        ?.let { COVER_BEFORE + controlField.substring(it) + COVER_AFTER }

class BnfEditionLookup(baseUrl: String, timeout: Duration) : ExternalEditionLookup {
    override val source = BNF
    private val http = sourceRestClient(baseUrl, timeout)

    override fun lookUp(isbn: Isbn): ExternalLookupResult =
        try {
            answerFor(isbn)
        } catch (ignored: RestClientException) {
            Failed
        } catch (ignored: SAXException) {
            Failed
        }

    private fun answerFor(isbn: Isbn): ExternalLookupResult =
        recordIn(search(isbn))?.let { answerFrom(isbn, it) } ?: NothingKnown

    private fun answerFrom(isbn: Isbn, record: UnimarcRecord): ExternalLookupResult =
        record.value("200", "a")?.let { Known(previewOf(isbn, it, record)) } ?: Failed

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
            coverUrl = coverUrlOf(record.control("003")),
        )
    }

    private fun publicationOf(record: UnimarcRecord): UnimarcField? =
        record.field("214", PUBLISHER_INDICATOR) ?: record.field("210")

    private fun contributionsOf(record: UnimarcRecord): List<Contribution> =
        record.fields(AUTHOR_TAGS).mapNotNull { field ->
            Contribution.of(nameOf(field), contributionRoleOf(field.value("4")))
        }

    private fun nameOf(field: UnimarcField): String =
        "${field.value("b").orEmpty()} ${field.value("a").orEmpty()}".trim()

    private fun seriesOf(record: UnimarcRecord): SeriesEntry? =
        SeriesEntry.of(record.value("461", "t"), record.value("461", "v")?.toIntOrNull())
            ?: titleStatementSeriesOf(record)

    private fun titleStatementSeriesOf(record: UnimarcRecord): SeriesEntry? =
        tomeOf(record.value("200", "h"))?.let { tome -> SeriesEntry.of(record.value("200", "a"), tome) }

    private fun search(isbn: Isbn): String = http.get()
        .uri { uri ->
            uri.queryParam("version", "1.2")
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
    private val fields: List<UnimarcField>,
) {
    fun control(tag: String): String? = controls.firstOrNull { it.first == tag }?.second

    fun value(tag: String, code: String): String? = field(tag)?.value(code)

    fun field(tag: String): UnimarcField? = fields.firstOrNull { it.tag == tag }

    fun field(tag: String, indicator2: String): UnimarcField? =
        fields.firstOrNull { it.tag == tag && it.indicator2 == indicator2 }

    fun fields(tags: Set<String>): List<UnimarcField> = fields.filter { it.tag in tags }
}

private class UnimarcField(
    val tag: String,
    val indicator2: String,
    private val subfields: List<Pair<String, String>>,
) {
    fun value(code: String): String? = subfields.firstOrNull { it.first == code }?.second
}
