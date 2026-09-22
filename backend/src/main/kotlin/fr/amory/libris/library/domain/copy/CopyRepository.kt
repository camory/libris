package fr.amory.libris.library.domain.copy

interface CopyRepository {
    fun insert(copy: Copy)
}
