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
import fr.amory.libris.bibliography.domain.lookup.EditionPreview

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

internal fun UnimarcRecord.previewFor(isbn: Isbn): EditionPreview? =
  value("200", "a")?.let { title ->
    EditionPreview(
      isbn = isbn,
      kind = kindOf(value("105", "a"), value("101", "c")),
      title = title,
      subtitle = value("200", "e"),
      contributions = contributions,
      series = series,
      collection = value("410", "t"),
      publisher = publication?.value("c"),
      publicationYear = publicationYearOf(value("100", "a"), publication?.value("d")),
      language = LANGUAGES[value("101", "a")],
      pageCount = pageCountOf(value("215", "a")),
      summary = null,
    )
  }

internal val UnimarcRecord.ark: String?
  get() = arkOf(control("003"))

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

internal fun arkOf(controlField: String?): String? =
  controlField
    ?.indexOf(ARK)
    ?.takeIf { it >= 0 }
    ?.let { controlField.substring(it) }

private val UnimarcRecord.contributions: Contributions
  get() = Contributions.of(
    fields(AUTHOR_TAGS).mapNotNull { field -> Contribution.of(field.name, contributionRoleOf(field.value("4"))) },
  )

private val UnimarcRecord.series: SeriesEntry?
  get() = SeriesEntry.of(value("461", "t"), value("461", "v")?.toIntOrNull()) ?: titleStatementSeries

private val UnimarcRecord.titleStatementSeries: SeriesEntry?
  get() = tomeOf(value("200", "h"))?.let { tome -> SeriesEntry.of(value("200", "a"), tome) }

private val UnimarcRecord.publication: UnimarcField?
  get() = field("214", PUBLISHER_INDICATOR) ?: field("210")

private val UnimarcField.name: String
  get() = "${value("b").orEmpty()} ${value("a").orEmpty()}".trim()
