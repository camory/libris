package fr.amory.libris.web.copy

import fr.amory.libris.library.application.RemoveCopy
import fr.amory.libris.library.application.RemoveCopyResult.NoSuchCopy
import fr.amory.libris.library.application.RemoveCopyResult.Removed
import fr.amory.libris.library.domain.copy.CopyId
import fr.amory.libris.library.domain.reader.Reader
import fr.amory.libris.web.problem.NOT_FOUND_PROBLEM
import fr.amory.libris.web.problem.asResponse
import fr.amory.libris.web.problem.problem
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class CopyController(private val removeCopy: RemoveCopy) {
  @DeleteMapping("/api/v1/copies/{id}")
  fun remove(@AuthenticationPrincipal reader: Reader, @PathVariable("id") id: UUID): ResponseEntity<Any> =
    when (removeCopy(reader.id, CopyId(id))) {
      Removed    -> ResponseEntity.status(NO_CONTENT).build()
      NoSuchCopy -> problem(NOT_FOUND, NOT_FOUND_PROBLEM).asResponse()
    }
}
