package fr.amory.libris.bibliography.domain

data class SeriesEntry(
    val name: String,
    val volumeNumber: Int?,
) {
    init {
        require(name.isNotBlank()) { "a series entry needs a name" }
    }
}
