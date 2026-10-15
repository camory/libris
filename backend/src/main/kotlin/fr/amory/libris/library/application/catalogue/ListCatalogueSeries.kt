package fr.amory.libris.library.application.catalogue

import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class ListCatalogueSeries {
  @Suppress("UnusedParameter")
  operator fun invoke(readerId: ReaderId): List<CatalogueSeries> =
    emptyList()
}
