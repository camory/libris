package fr.amory.libris.bibliography.domain.lookup

import fr.amory.libris.bibliography.domain.cover.CoverSource

data class CoverCandidate(val source: CoverSource, val url: String)
