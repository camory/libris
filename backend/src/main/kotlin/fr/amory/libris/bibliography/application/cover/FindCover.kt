package fr.amory.libris.bibliography.application.cover

import fr.amory.libris.bibliography.domain.cover.Cover
import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.cover.CoverStore
import org.springframework.stereotype.Service

@Service
class FindCover(private val covers: CoverStore) {
  operator fun invoke(name: CoverName): Cover? =
    covers.read(name)
}
