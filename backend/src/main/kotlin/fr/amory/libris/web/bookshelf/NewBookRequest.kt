package fr.amory.libris.web.bookshelf

import fr.amory.libris.bibliography.application.edition.NewEdition
import fr.amory.libris.bibliography.domain.ContributionRole
import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.bibliography.domain.cover.CoverSource
import fr.amory.libris.web.bookshelf.NewBookValidation.Accepted
import fr.amory.libris.web.bookshelf.NewBookValidation.Refused
import fr.amory.libris.web.catalogue.EditionDetailsRequest
import fr.amory.libris.web.catalogue.EditionDetailsValidation
import fr.amory.libris.web.isbn.isbn13Of
import fr.amory.libris.web.problem.ValidationErrorResponse

sealed interface NewBookValidation {
  data class Accepted(val newEdition: NewEdition) : NewBookValidation

  data class Refused(val errors: List<ValidationErrorResponse>) : NewBookValidation
}

data class NewAuthorRequest(
  val name: String,
  val role: ContributionRole)

data class NewSeriesRequest(
  val name: String,
  val volumeNumber: Int?)

data class NewBookRequest(
  val isbn13: String?,
  val kind: Kind,
  val title: String,
  val subtitle: String?,
  val authors: List<NewAuthorRequest>,
  val series: NewSeriesRequest?,
  val collection: String?,
  val publisher: String?,
  val publicationYear: Int?,
  val language: String?,
  val pageCount: Int?,
  val summary: String?,
  val coverSource: String?) {
  fun validate(): NewBookValidation {
    val isbn = isbn13?.let { isbn13Of(it) }
    val validation = editionDetails().validate()
    val errors = buildList {
      if (isbn13 != null && isbn == null) add(ValidationErrorResponse("isbn13", "not-an-isbn"))
      if (validation is EditionDetailsValidation.Refused) addAll(validation.errors)
    }
    return if (validation is EditionDetailsValidation.Accepted && errors.isEmpty()) {
      Accepted(NewEdition(isbn, validation.details, coverSource?.let { CoverSource.of(it) }))
    } else {
      Refused(errors)
    }
  }

  private fun editionDetails(): EditionDetailsRequest =
    EditionDetailsRequest(
      kind = kind,
      title = title,
      subtitle = subtitle,
      authors = authors,
      series = series,
      collection = collection,
      publisher = publisher,
      publicationYear = publicationYear,
      language = language,
      pageCount = pageCount,
      summary = summary,
    )
}
