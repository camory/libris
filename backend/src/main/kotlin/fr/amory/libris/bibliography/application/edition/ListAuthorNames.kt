package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.domain.edition.AuthorNames
import org.springframework.stereotype.Service

@Service
class ListAuthorNames(private val authorNames: AuthorNames) {
  operator fun invoke(): List<String> =
    authorNames.findAll()
}
