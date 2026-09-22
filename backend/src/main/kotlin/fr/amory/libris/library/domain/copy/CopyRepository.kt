package fr.amory.libris.library.domain.copy

import fr.amory.libris.bibliography.domain.edition.EditionId

interface CopyRepository {
    fun insert(copy: Copy)

    fun findByEditionId(editionId: EditionId): List<Copy>
}
