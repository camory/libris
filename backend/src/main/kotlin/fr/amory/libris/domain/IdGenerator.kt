package fr.amory.libris.domain

import java.util.UUID

interface IdGenerator {
    fun next(): UUID
}
