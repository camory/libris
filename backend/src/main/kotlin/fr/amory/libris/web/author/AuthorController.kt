package fr.amory.libris.web.author

import fr.amory.libris.bibliography.application.edition.ListAuthorNames
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthorController(private val listAuthorNames: ListAuthorNames) {
  @GetMapping("/api/v1/author-names")
  fun names(): List<String> =
    listAuthorNames()
}
