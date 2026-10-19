import type { Book } from "../../domain/Book";
import type { AuthorRole, Kind } from "../../domain/SourceEdition";

export interface BookResponse {
  id: string;
  isbn13: string | null;
  kind: Kind;
  title: string;
  subtitle: string | null;
  authors: { name: string; role: AuthorRole }[];
  series: { name: string; volumeNumber: number | null } | null;
  collection: string | null;
  publisher: string | null;
  publicationYear: number | null;
  language: string | null;
  pageCount: number | null;
  summary: string | null;
  coverUrl: string | null;
  copies: { id: string; bookshelf: { id: string; name: string } }[];
}

export function bookOf(book: BookResponse): Book {
  return {
    id: book.id,
    isbn13: book.isbn13,
    kind: book.kind,
    title: book.title,
    subtitle: book.subtitle,
    authors: book.authors.map((author) => ({
      name: author.name,
      role: author.role,
    })),
    series: book.series,
    collection: book.collection,
    publisher: book.publisher,
    publicationYear: book.publicationYear,
    language: book.language,
    pageCount: book.pageCount,
    summary: book.summary,
    coverUrl: book.coverUrl,
    copies: book.copies.map((copy) => ({
      id: copy.id,
      bookshelf: { id: copy.bookshelf.id, name: copy.bookshelf.name },
    })),
  };
}
