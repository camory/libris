package fr.amory.libris.bibliography.domain

@JvmInline
value class Contributions private constructor(private val all: List<Contribution>) : Iterable<Contribution> {
    override fun iterator(): Iterator<Contribution> = all.iterator()

    fun isEmpty(): Boolean = all.isEmpty()

    companion object {
        fun of(contributions: List<Contribution>): Contributions = Contributions(
            contributions
                .distinctBy { it.name.lowercase() to it.role }
                .sortedWith(compareBy({ it.role.order }, { it.name.lowercase() })),
        )
    }
}
