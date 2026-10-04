package fr.amory.libris.bibliography.application.lookup

import fr.amory.libris.bibliography.domain.cover.CoverName
import fr.amory.libris.bibliography.domain.edition.EditionId
import fr.amory.libris.bibliography.domain.lookup.CoverCandidates
import fr.amory.libris.bibliography.domain.lookup.EditionPreview

sealed class EditionLookupResult {
  data class Held(val id: EditionId, val preview: EditionPreview, val coverName: CoverName?) : EditionLookupResult()

  data class Found(val preview: EditionPreview, val covers: CoverCandidates) : EditionLookupResult()

  data object UnknownIsbn : EditionLookupResult()

  data object SourcesUnavailable : EditionLookupResult()
}
