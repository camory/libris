package fr.amory.libris.infra.persistence

import fr.amory.libris.domain.Reader
import fr.amory.libris.domain.ReaderRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JdbcReaderRepository(private val jdbcClient: JdbcClient) : ReaderRepository {
    override fun insert(reader: Reader) {
        jdbcClient
            .sql("insert into reader (id, username, email, display_name) values (:id, :username, :email, :displayName)")
            .param("id", reader.id)
            .param("username", reader.username)
            .param("email", reader.email)
            .param("displayName", reader.displayName)
            .update()
    }

    override fun findByUsername(username: String): Reader? =
        jdbcClient
            .sql("select id, username, email, display_name from reader where username = :username")
            .param("username", username)
            .query { rs, _ ->
                Reader(
                    id = rs.getObject("id", UUID::class.java),
                    username = rs.getString("username"),
                    email = rs.getString("email"),
                    displayName = rs.getString("display_name"),
                )
            }
            .optional()
            .orElse(null)
}
