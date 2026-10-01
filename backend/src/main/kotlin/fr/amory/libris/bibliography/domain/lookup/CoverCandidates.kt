package fr.amory.libris.bibliography.domain.lookup

@JvmInline
value class CoverCandidates private constructor(private val all: List<CoverCandidate>) : Iterable<CoverCandidate> {
  override fun iterator(): Iterator<CoverCandidate> = all.iterator()

  companion object {
    fun of(candidates: List<CoverCandidate>): CoverCandidates =
      CoverCandidates(candidates.sortedBy { it.source.order })
  }
}
