package fr.amory.libris.domain.lookup

fun merge(editions: List<SourceEdition>): SourceEdition = editions.first()
