package fr.amory.libris.web

import fr.amory.libris.bibliography.domain.cover.CoverName

internal fun coverPathOf(name: CoverName): String =
  "/api/v1/covers/${name.value}"
