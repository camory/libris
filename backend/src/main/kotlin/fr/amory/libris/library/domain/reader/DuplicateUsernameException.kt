package fr.amory.libris.library.domain.reader

class DuplicateUsernameException(username: String, cause: Throwable? = null) :
    RuntimeException("A reader named $username already exists", cause)
