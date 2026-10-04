package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.Isbn
import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverFetch
import fr.amory.libris.bibliography.domain.cover.CoverSource

class CoverFetchAnswering(override val coverSource: CoverSource, private val cover: Cover?) : CoverFetch {
  private val isbns = mutableListOf<Isbn>()

  val asked: List<Isbn> get() = isbns.toList()

  override fun fetch(isbn: Isbn): Cover? {
    isbns += isbn
    return cover
  }
}
