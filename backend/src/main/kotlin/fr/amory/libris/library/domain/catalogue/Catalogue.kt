package fr.amory.libris.library.domain.catalogue

import fr.amory.libris.library.domain.reader.ReaderId

interface Catalogue {
    fun editionsHeldBy(readerId: ReaderId): List<CatalogueEdition>
}
