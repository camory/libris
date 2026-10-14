package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.bookshelf.BookshelfName
import fr.amory.libris.library.domain.bookshelf.BookshelfRepository
import fr.amory.libris.library.domain.bookshelf.Membership
import fr.amory.libris.library.domain.bookshelf.MembershipRole
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import java.util.UUID

private const val INSERT_BOOKSHELF =
  "INSERT INTO bookshelf (id, name, created_by, created_at) VALUES (:id, :name, :createdBy, :createdAt)"

private const val INSERT_MEMBERSHIP =
  "INSERT INTO membership (bookshelf_id, reader_id, role) VALUES (:bookshelfId, :readerId, :role)"

private const val UPDATE_BOOKSHELF = "UPDATE bookshelf SET name = :name WHERE id = :id"

private const val DELETE_MEMBERSHIPS = "DELETE FROM membership WHERE bookshelf_id = :bookshelfId"

private const val FIND_BOOKSHELF_BY_ID =
  """
    SELECT bookshelf.id, bookshelf.name, bookshelf.created_by, bookshelf.created_at, membership.reader_id, membership.role
    FROM bookshelf
    JOIN membership ON membership.bookshelf_id = bookshelf.id
    WHERE bookshelf.id = :id
    ORDER BY membership.reader_id
    """

private const val FIND_BOOKSHELVES_BY_MEMBER =
  """
    SELECT bookshelf.id, bookshelf.name, bookshelf.created_by, bookshelf.created_at, membership.reader_id, membership.role
    FROM bookshelf
    JOIN membership ON membership.bookshelf_id = bookshelf.id
    WHERE bookshelf.id IN (SELECT member.bookshelf_id FROM membership member WHERE member.reader_id = :readerId)
    ORDER BY bookshelf.id, membership.reader_id
    """

private class BookshelfRow(
  val id: BookshelfId,
  val name: BookshelfName,
  val createdBy: ReaderId,
  val createdAt: Instant,
  val membership: Membership)

@Repository
class JdbcBookshelfRepository(private val jdbcClient: JdbcClient) : BookshelfRepository {
  override fun insert(bookshelf: Bookshelf) {
    jdbcClient
      .sql(INSERT_BOOKSHELF)
      .param("id", bookshelf.id.value)
      .param("name", bookshelf.name.value)
      .param("createdBy", bookshelf.createdBy.value)
      .param("createdAt", bookshelf.createdAt.atOffset(UTC))
      .update()
    insertMemberships(bookshelf)
  }

  override fun update(bookshelf: Bookshelf) {
    jdbcClient
      .sql(UPDATE_BOOKSHELF)
      .param("id", bookshelf.id.value)
      .param("name", bookshelf.name.value)
      .update()
    jdbcClient
      .sql(DELETE_MEMBERSHIPS)
      .param("bookshelfId", bookshelf.id.value)
      .update()
    insertMemberships(bookshelf)
  }

  private fun insertMemberships(bookshelf: Bookshelf) {
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

  private fun rowOf(rs: ResultSet): BookshelfRow =
    BookshelfRow(
      id = BookshelfId(rs.getObject("id", UUID::class.java)),
      name = BookshelfName(rs.getString("name")),
      createdBy = ReaderId(rs.getObject("created_by", UUID::class.java)),
      createdAt = rs.getObject("created_at", OffsetDateTime::class.java).toInstant(),
      membership = Membership(
        ReaderId(rs.getObject("reader_id", UUID::class.java)),
        MembershipRole.valueOf(rs.getString("role")),
      ),
    )

  private fun bookshelfOf(rows: List<BookshelfRow>): Bookshelf =
    rows.first().let { Bookshelf(it.id, it.name, rows.map { row -> row.membership }, it.createdBy, it.createdAt) }
}
