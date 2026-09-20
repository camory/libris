package fr.amory.libris.library.domain.reader

interface ReaderRepository {
    fun insert(reader: Reader)

    fun findByUsername(username: String): Reader?
}
