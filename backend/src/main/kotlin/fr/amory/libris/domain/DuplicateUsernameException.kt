package fr.amory.libris.domain

class DuplicateUsernameException(username: String, cause: Throwable? = null) :
    RuntimeException("A reader named $username already exists", cause)
