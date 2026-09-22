package fr.amory.libris.library.fixture

import fr.amory.libris.library.domain.copy.Copy
import fr.amory.libris.library.domain.copy.CopyRepository

class CopiesInMemory : CopyRepository {
    private val copies = mutableListOf<Copy>()

    val stored: List<Copy> get() = copies.toList()

    override fun insert(copy: Copy) {
        copies += copy
    }
}
