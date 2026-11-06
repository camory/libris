package fr.amory.libris.library.application

sealed class RemoveCopyResult {
  data object Removed : RemoveCopyResult()

  data object NoSuchCopy : RemoveCopyResult()
}
