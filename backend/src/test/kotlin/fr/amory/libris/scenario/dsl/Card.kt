package fr.amory.libris.scenario.dsl

data class Card(val isbn13: String, val title: String, val copies: List<Copy>)

fun Reader.asks(isbn: String): Answer<Card> =
  libris.get(this, "/api/v1/isbn/$isbn") { read<Card>(it) }
