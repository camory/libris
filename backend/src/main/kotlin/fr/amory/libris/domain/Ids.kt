package fr.amory.libris.domain

import com.fasterxml.uuid.Generators
import java.util.UUID

fun newId(): UUID = Generators.timeBasedEpochGenerator().generate()
