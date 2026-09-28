import type { Book } from "../domain/Book";
import type { Bookshelf } from "../domain/Bookshelf";

export const bibliothequeDeLea: Bookshelf = {
  id: "0b1e2d3c-4f5a-4b6c-8d7e-9f0a1b2c3d4e",
  name: "Bibliothèque de Léa",
};

export const salon: Bookshelf = {
  id: "1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f",
  name: "Salon",
};

export const asterixLeGaulois: Book = {
  id: "6f1d2c3b-4a59-4e6f-8b70-1c2d3e4f5a61",
  isbn13: null,
  kind: "BD",
  title: "Astérix le Gaulois",
  subtitle: null,
  authors: [
    { name: "René Goscinny", role: "WRITER" },
    { name: "Albert Uderzo", role: "ARTIST" },
  ],
  series: { name: "Astérix", volumeNumber: 1 },
  collection: null,
  publisher: null,
  publicationYear: null,
  language: null,
  pageCount: null,
  summary: null,
  coverUrl: null,
  copies: [
    {
      id: "8b3f4e5d-6c7b-4a81-8d92-3e4f5a6b7c83",
      bookshelf: bibliothequeDeLea,
    },
  ],
};

export const asterixEtSesAmis: Book = {
  id: "7a2e3d4c-5b6a-4f70-9c81-2d3e4f5a6b72",
  isbn13: null,
  kind: "BD",
  title: "Astérix et ses amis",
  subtitle: null,
  authors: [
    { name: "René Goscinny", role: "WRITER" },
    { name: "Albert Uderzo", role: "ARTIST" },
  ],
  series: { name: "Astérix", volumeNumber: null },
  collection: null,
  publisher: null,
  publicationYear: null,
  language: null,
  pageCount: null,
  summary: null,
  coverUrl: null,
  copies: [{ id: "cf7d8c9b-a01f-4ec5-a1d6-7c8d9eafb0c7", bookshelf: salon }],
};

export const romanceDawn: Book = {
  id: "5e0c1b2a-3948-4d5e-8a6f-0b1c2d3e4f50",
  isbn13: null,
  kind: "MANGA",
  title: "Romance dawn",
  subtitle: null,
  authors: [
    { name: "Eiichirō Oda", role: "WRITER" },
    { name: "Eiichirō Oda", role: "ARTIST" },
  ],
  series: { name: "One piece", volumeNumber: 1 },
  collection: null,
  publisher: null,
  publicationYear: null,
  language: null,
  pageCount: null,
  summary: null,
  coverUrl: "https://covers.openlibrary.org/b/isbn/9782723488525-L.jpg",
  copies: [
    {
      id: "9c4a5f6e-7d8c-4b92-9ea3-4f5a6b7c8d94",
      bookshelf: bibliothequeDeLea,
    },
    {
      id: "ad5b6a7f-8e9d-4ca3-8fb4-5a6b7c8d9ea5",
      bookshelf: bibliothequeDeLea,
    },
    { id: "be6c7b8a-9f0e-4db4-90c5-6b7c8d9eafb6", bookshelf: salon },
  ],
};
