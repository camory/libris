import type { Copy } from "./Copy";
import { nameOrder } from "./nameOrder";
import type { Kind, SourceAuthor, SourceSeries } from "./SourceEdition";

export interface Book {
  id: string;
  isbn13: string | null;
  kind: Kind;
  title: string;
  subtitle: string | null;
  authors: SourceAuthor[];
  series: SourceSeries | null;
  collection: string | null;
  publisher: string | null;
  publicationYear: number | null;
  language: string | null;
  pageCount: number | null;
  summary: string | null;
  coverUrl: string | null;
  copies: Copy[];
}

export function booksInOrder(books: Book[], locale: string): Book[] {
  const byTitle = nameOrder(locale);
  return [...books].sort((a, b) => byTome(a, b) || byTitle(a.title, b.title));
}

function byTome(a: Book, b: Book): number {
  if (tomeOf(a) === tomeOf(b)) {
    return 0;
  }
  return tomeOf(a) - tomeOf(b);
}

function tomeOf(book: Book): number {
  return book.series?.volumeNumber ?? Infinity;
}
