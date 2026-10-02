package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.edition.EditionId

data class CataloguePage(
  val held: List<HeldEdition>,
  val next: EditionId?)
