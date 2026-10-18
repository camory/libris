package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.SeriesEntry
import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.library.application.lookup.CopyOnBookshelf

data class HeldEdition(
  val edition: Edition,
  val series: SeriesEntry?,
  val copies: List<CopyOnBookshelf>)
