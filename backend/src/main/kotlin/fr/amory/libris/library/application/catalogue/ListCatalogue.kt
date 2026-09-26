package fr.amory.libris.library.application.catalogue

import fr.amory.libris.library.domain.catalogue.Catalogue
import fr.amory.libris.library.domain.catalogue.CatalogueEdition
import fr.amory.libris.library.domain.reader.ReaderId
import org.springframework.stereotype.Service

@Service
class ListCatalogue(private val catalogue: Catalogue) {
    operator fun invoke(readerId: ReaderId): List<CatalogueEdition> = catalogue.editionsHeldBy(readerId)
}
