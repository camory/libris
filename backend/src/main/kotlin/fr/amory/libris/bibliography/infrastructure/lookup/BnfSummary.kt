package fr.amory.libris.bibliography.infrastructure.lookup

private val TAG = Regex("<[^>]*>")

internal fun summaryOf(description: String?): String? =
  description?.replace(TAG, "")
