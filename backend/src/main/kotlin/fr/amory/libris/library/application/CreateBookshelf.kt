package fr.amory.libris.library.application

import fr.amory.libris.library.application.CreateBookshelfResult.Created
import fr.amory.libris.library.application.CreateBookshelfResult.NameTaken
import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole.OWNER
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionOperations

@Service
class CreateBookshelf(
  private val bookshelves: BookshelfRepository,
  private val transactions: TransactionOperations) {
  operator fun invoke(readerId: ReaderId, name: BookshelfName): CreateBookshelfResult {
    if (bookshelves.findByMember(readerId).any { it.name.isSameAs(name) }) return NameTaken
    val bookshelf = Bookshelf(BookshelfId.new(), name, listOf(Membership(readerId, OWNER)))
    transactions.executeWithoutResult { bookshelves.insert(bookshelf) }
    return Created(bookshelf)
  }
}
