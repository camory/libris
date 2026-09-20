package fr.amory.libris.library.domain.bookshelf

data class Bookshelf(
    val id: BookshelfId,
    val name: String,
    val memberships: List<Membership>,
)
