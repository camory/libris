package fr.amory.libris.library.application

sealed class AddMemberResult {
  data class Added(val member: BookshelfMember) : AddMemberResult()

  data object NoSuchBookshelf : AddMemberResult()

  data object UnknownReader : AddMemberResult()

  data object AlreadyAMember : AddMemberResult()
}
