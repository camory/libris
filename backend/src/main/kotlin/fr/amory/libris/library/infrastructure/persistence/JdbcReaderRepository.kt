package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.library.domain.bookshelf.BookshelfId
import fr.amory.libris.library.domain.reader.DuplicateUsernameException
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.domain.reader.ReaderRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.UUID

private const val INSERT_READER =
  "INSERT INTO reader (id, username, email, display_name, default_bookshelf_id) " +
    "VALUES (:id, :username, :email, :displayName, :defaultBookshelfId)"

private const val UPDATE_READER =
  "UPDATE reader SET username = :username, email = :email, display_name = :displayName, " +
    "default_bookshelf_id = :defaultBookshelfId WHERE id = :id"

private const val FIND_READER_BY_USERNAME =
  "SELECT id, username, email, display_name, default_bookshelf_id FROM reader WHERE username = :username"

private const val FIND_READER_BY_ID =
  "SELECT id, username, email, display_name, default_bookshelf_id FROM reader WHERE id = :id"

private const val FIND_READERS_BY_IDS =
  "SELECT id, username, email, display_name, default_bookshelf_id FROM reader WHERE id IN (:ids)"

private const val FIND_ALL_READERS =
  "SELECT id, username, email, display_name, default_bookshelf_id FROM reader"

@Repository
class JdbcReaderRepository(private val jdbcClient: JdbcClient) : ReaderRepository {
  override fun insert(reader: Reader) {
    try {
      jdbcClient
        .sql(INSERT_READER)
        .param("id", reader.id.value)
        .param("username", reader.username)
        .param("email", reader.email)
        .param("displayName", reader.displayName)
        .param("defaultBookshelfId", reader.defaultBookshelfId.value)
        .update()
    } catch (duplicate: DuplicateKeyException) {
      throw DuplicateUsernameException(reader.username, duplicate)
    }
  }

  override fun update(reader: Reader) {
    jdbcClient
      .sql(UPDATE_READER)
      .param("id", reader.id.value)
      .param("username", reader.username)
      .param("email", reader.email)
      .param("displayName", reader.displayName)
      .param("defaultBookshelfId", reader.defaultBookshelfId.value)
      .update()
  }

  override fun findByUsername(username: String): Reader? =
    jdbcClient
      .sql(FIND_READER_BY_USERNAME)
      .param("username", username)
      .query { rs, _ -> readerOf(rs) }
      .optional()
      .orElse(null)

  override fun findById(id: ReaderId): Reader? =
    jdbcClient
      .sql(FIND_READER_BY_ID)
      .param("id", id.value)
      .query { rs, _ -> readerOf(rs) }
      .optional()
      .orElse(null)

  override fun findByIds(ids: List<ReaderId>): List<Reader> =
    if (ids.isEmpty()) {
      emptyList()
    } else {
      jdbcClient
        .sql(FIND_READERS_BY_IDS)
        .param("ids", ids.map { it.value })
        .query { rs, _ -> readerOf(rs) }
        .list()
    }

  override fun findAll(): List<Reader> =
    jdbcClient
      .sql(FIND_ALL_READERS)
      .query { rs, _ -> readerOf(rs) }
      .list()

  private fun readerOf(rs: ResultSet): Reader =
    Reader(
      id = ReaderId(rs.getObject("id", UUID::class.java)),
      username = rs.getString("username"),
      email = rs.getString("email"),
      displayName = rs.getString("display_name"),
      defaultBookshelfId = BookshelfId(rs.getObject("default_bookshelf_id", UUID::class.java)),
    )
}
