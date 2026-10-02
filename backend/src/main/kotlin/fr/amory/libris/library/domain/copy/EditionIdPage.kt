package fr.amory.libris.library.domain.copy

import fr.amory.libris.bibliography.domain.edition.EditionId

data class EditionIdPage(
  val editionIds: List<EditionId>,
  val next: EditionId?)
