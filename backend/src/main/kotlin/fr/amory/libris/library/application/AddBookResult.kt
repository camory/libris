package fr.amory.libris.library.application

import fr.amory.libris.library.domain.bookshelf.Bookshelf
import fr.amory.libris.library.domain.copy.Copy

sealed class AddBookResult {
    data class Added(val copy: Copy, val bookshelf: Bookshelf) : AddBookResult()
}
