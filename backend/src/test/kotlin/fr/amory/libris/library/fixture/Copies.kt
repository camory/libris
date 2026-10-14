package fr.amory.libris.library.fixture

import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.fixture.MutableClock
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyId

fun copyOn(bookshelf: Bookshelf, editionId: EditionId): Copy =
  Copy(CopyId.new(), editionId, bookshelf.id, bookshelf.createdBy, MutableClock().instant())
