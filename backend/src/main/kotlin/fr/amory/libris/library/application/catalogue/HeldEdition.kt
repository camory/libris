package fr.amory.libris.library.application.catalogue

import fr.amory.libris.bibliography.domain.edition.Edition
import fr.amory.libris.bibliography.domain.series.Series
import fr.amory.libris.library.application.lookup.CopyOnBookshelf

data class HeldEdition(
  val edition: Edition,
  val series: Series?,
  val copies: List<CopyOnBookshelf>)
