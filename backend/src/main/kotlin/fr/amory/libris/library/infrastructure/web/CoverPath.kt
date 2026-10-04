package fr.amory.libris.library.infrastructure.web

import fr.amory.libris.bibliography.domain.cover.CoverName

internal fun coverPathOf(name: CoverName): String =
  "/api/v1/covers/${name.value}"
