package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.cover.CoverSource

data class NewEdition(
  val isbn: Isbn?,
  val details: EditionDetails,
  val coverSource: CoverSource?)
