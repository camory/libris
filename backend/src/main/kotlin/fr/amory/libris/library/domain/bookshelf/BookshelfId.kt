package fr.amory.libris.library.domain.bookshelf

import com.fasterxml.uuid.Generators
import java.util.UUID

@JvmInline
value class BookshelfId(val value: UUID) {
    companion object {
        fun new(): BookshelfId = BookshelfId(Generators.timeBasedEpochGenerator().generate())
    }
}
