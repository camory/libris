import type { SourceEdition } from "../domain/SourceEdition";

export const onePiece1: SourceEdition = {
  isbn13: "9782723488525",
  kind: "MANGA",
  title: "Romance dawn",
  subtitle: "à l'aube d'une grande aventure",
  authors: [
    { name: "Eiichirō Oda", role: "WRITER" },
    { name: "Eiichirō Oda", role: "ARTIST" },
  ],
  series: { name: "One piece", volumeNumber: 1 },
  collection: "Shonen manga",
  publisher: "Glénat",
  publicationYear: 2013,
  language: "fr",
  pageCount: 203,
  summary: null,
  coverUrl: "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
};

export const onePiece2: SourceEdition = {
  isbn13: "9782723489898",
  kind: "MANGA",
  title: "Aux prises avec Baggy et ses hommes",
  subtitle: null,
  authors: [
    { name: "Eiichirō Oda", role: "WRITER" },
    { name: "Eiichirō Oda", role: "ARTIST" },
  ],
  series: { name: "One piece", volumeNumber: 2 },
  collection: "Shonen manga",
  publisher: "Glénat",
  publicationYear: 2013,
  language: "fr",
  pageCount: 208,
  summary: null,
  coverUrl: "https://covers.openlibrary.org/b/isbn/9782723489898-L.jpg",
};
