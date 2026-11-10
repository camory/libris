package fr.amory.libris.bibliography.application.edition

import fr.amory.libris.bibliography.domain.edition.Edition

sealed class EditEditionResult {
  data class Edited(val edition: Edition) : EditEditionResult()

  data object NoSuchEdition : EditEditionResult()
}
