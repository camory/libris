package fr.amory.libris.bibliography.domain.cover

interface CoverStore {
  fun read(name: CoverName): Cover?

  fun write(cover: Cover)
}
