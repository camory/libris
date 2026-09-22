package fr.amory.libris.bibliography.domain

@JvmInline
value class Contributions private constructor(val all: List<Contribution>) {
    companion object {
        fun of(contributions: List<Contribution>): Contributions = Contributions(
            contributions
                .distinctBy { it.name.lowercase() to it.role }
                .sortedWith(compareBy({ it.role.order }, { it.name.lowercase() })),
        )
    }
}
