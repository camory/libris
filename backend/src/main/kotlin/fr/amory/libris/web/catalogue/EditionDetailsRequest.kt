package fr.amory.libris.web.catalogue

import fr.amory.libris.bibliography.domain.Kind
import fr.amory.libris.web.bookshelf.NewAuthorRequest
import fr.amory.libris.web.bookshelf.NewSeriesRequest

data class EditionDetailsRequest(
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
  val summary: String?)
