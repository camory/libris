package fr.amory.libris.domain

interface ReaderRepository {
    fun insert(reader: Reader)

    fun findByUsername(username: String): Reader?
}
