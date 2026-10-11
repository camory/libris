package fr.amory.libris.web.reader

import fr.amory.libris.library.application.ListReaders
import fr.amory.libris.library.domain.reader.Reader
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ReaderController(private val listReaders: ListReaders) {
  @GetMapping("/api/v1/readers")
  fun list(): List<ReaderResponse> =
    listReaders().map { ReaderResponse.from(it) }
}

data class ReaderResponse(
  val id: String,
  val displayName: String) {
  companion object {
    fun from(reader: Reader): ReaderResponse =
      ReaderResponse(reader.id.value.toString(), reader.displayName)
  }
}
