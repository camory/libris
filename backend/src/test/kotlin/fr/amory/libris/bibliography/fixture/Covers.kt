package fr.amory.libris.bibliography.fixture

import fr.amory.libris.bibliography.domain.cover.Cover

fun coverOf(mediaType: String, bytes: ByteArray): Cover =
  checkNotNull(Cover.of(mediaType, bytes))
