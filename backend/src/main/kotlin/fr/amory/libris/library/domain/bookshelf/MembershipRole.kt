package fr.amory.libris.library.domain.bookshelf

enum class MembershipRole(val order: Int) {
  OWNER(1),
  VIEWER(2)
}
