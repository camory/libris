package fr.amory.libris.library.infrastructure.persistence

import fr.amory.libris.library.domain.reader.DuplicateUsernameException
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.library.domain.reader.ReaderId
import fr.amory.libris.library.domain.reader.ReaderRepository
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

private const val INSERT_READER =
    "insert into reader (id, username, email, display_name) values (:id, :username, :email, :displayName)"

private const val FIND_READER_BY_USERNAME =
    "select id, username, email, display_name from reader where username = :username"

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
                .update()
        } catch (duplicate: DuplicateKeyException) {
            throw DuplicateUsernameException(reader.username, duplicate)
        }
    }

    override fun findByUsername(username: String): Reader? =
        jdbcClient
            .sql(FIND_READER_BY_USERNAME)
            .param("username", username)
            .query { rs, _ ->
                Reader(
                    id = ReaderId(rs.getObject("id", UUID::class.java)),
                    username = rs.getString("username"),
                    email = rs.getString("email"),
                    displayName = rs.getString("display_name"),
                )
            }
            .optional()
            .orElse(null)
}
