package fr.amory.libris.bibliography.domain

data class Contribution(
    val name: String,
    val role: ContributionRole,
) {
    init {
        require(name.isNotBlank()) { "a contribution needs a name" }
    }

    companion object {
        fun of(name: String?, role: ContributionRole): Contribution? =
            name?.takeUnless { it.isBlank() }?.let { Contribution(it, role) }
    }
}
