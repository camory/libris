package fr.amory.libris.scenario.dsl

data class Author(val name: String, val role: String)

data class SeriesEntry(val name: String, val volumeNumber: Int?)
