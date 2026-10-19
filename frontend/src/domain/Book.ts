import type { Copy } from "./Copy";
import { nameOrder } from "./nameOrder";
import { runsOf, type Run } from "./Run";
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

export type BookOrGap = { book: Book } | { gap: Run };

export function booksAndGapsInOrder(
  books: Book[],
  locale: string,
): BookOrGap[] {
  const gapBefore = new Map(
    runsOf(books).missing.map((gap) => [gap.last + 1, gap]),
  );
  return booksInOrder(books, locale).flatMap((book) => {
    const gap = gapBefore.get(tomeOf(book));
    gapBefore.delete(tomeOf(book));
    return gap === undefined ? [{ book }] : [{ gap }, { book }];
  });
}

export function publishersOf(books: Book[], locale: string): string[] {
  return [...new Set(books.map(({ publisher }) => publisher))].filter(
    (publisher) => publisher !== null,
  );
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
