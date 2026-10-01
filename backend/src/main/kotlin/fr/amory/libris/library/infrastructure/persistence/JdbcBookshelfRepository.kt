package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

private const val INSERT_BOOKSHELF = "INSERT INTO bookshelf (id, name) VALUES (:id, :name)"

private const val INSERT_MEMBERSHIP =
  "INSERT INTO membership (bookshelf_id, reader_id, role) VALUES (:bookshelfId, :readerId, :role)"

private const val FIND_BOOKSHELF_BY_ID =
  """
    SELECT bookshelf.id, bookshelf.name, membership.reader_id, membership.role
    FROM bookshelf
    JOIN membership ON membership.bookshelf_id = bookshelf.id
    WHERE bookshelf.id = :id
    ORDER BY membership.reader_id
    """

private const val FIND_BOOKSHELVES_BY_MEMBER =
  """
    SELECT bookshelf.id, bookshelf.name, membership.reader_id, membership.role
    FROM bookshelf
    JOIN membership ON membership.bookshelf_id = bookshelf.id
    WHERE bookshelf.id IN (SELECT member.bookshelf_id FROM membership member WHERE member.reader_id = :readerId)
    ORDER BY bookshelf.id, membership.reader_id
    """

private class BookshelfRow(
  val id: BookshelfId,
  val name: String,
  val membership: Membership,
)

@Repository
class JdbcBookshelfRepository(private val jdbcClient: JdbcClient) : BookshelfRepository {
  override fun insert(bookshelf: Bookshelf) {
    jdbcClient
      .sql(INSERT_BOOKSHELF)
      .param("id", bookshelf.id.value)
      .param("name", bookshelf.name)
      .update()
    bookshelf.memberships.forEach { membership ->
      jdbcClient
        .sql(INSERT_MEMBERSHIP)
        .param("bookshelfId", bookshelf.id.value)
        .param("readerId", membership.readerId.value)
        .param("role", membership.role.name)
        .update()
    }
  }

  override fun findById(id: BookshelfId): Bookshelf? =
    jdbcClient
      .sql(FIND_BOOKSHELF_BY_ID)
      .param("id", id.value)
      .query { rs, _ -> rowOf(rs) }
      .list()
      .takeIf { it.isNotEmpty() }
      ?.let { bookshelfOf(it) }

  override fun findByMember(readerId: ReaderId): List<Bookshelf> =
    jdbcClient
      .sql(FIND_BOOKSHELVES_BY_MEMBER)
      .param("readerId", readerId.value)
      .query { rs, _ -> rowOf(rs) }
      .list()
      .groupBy { it.id }
      .values
      .map { bookshelfOf(it) }

  private fun rowOf(rs: ResultSet): BookshelfRow = BookshelfRow(
    id = BookshelfId(rs.getObject("id", UUID::class.java)),
    name = rs.getString("name"),
    membership = Membership(
      ReaderId(rs.getObject("reader_id", UUID::class.java)),
      MembershipRole.valueOf(rs.getString("role")),
    ),
  )

  private fun bookshelfOf(rows: List<BookshelfRow>): Bookshelf =
    Bookshelf(rows.first().id, rows.first().name, rows.map { it.membership })
}
