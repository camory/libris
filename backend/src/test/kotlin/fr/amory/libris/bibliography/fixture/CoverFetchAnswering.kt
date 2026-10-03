package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverFetch

class CoverFetchAnswering(private val cover: Cover?) : CoverFetch {
  private val isbns = mutableListOf<Isbn>()

  val asked: List<Isbn> get() = isbns.toList()

  override fun fetch(isbn: Isbn): Cover? {
    isbns += isbn
    return cover
  }
}
