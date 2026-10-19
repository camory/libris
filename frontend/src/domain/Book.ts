import type { Copy } from "./Copy";
import { nameOrder } from "./nameOrder";
import { runsOf, type Run } from "./Run";
import {
  authorsInOrder,
  type Kind,
  type SourceAuthor,
  type SourceSeries,
} from "./SourceEdition";

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
  const byName = nameOrder(locale);
  const spelled = firstSpelling(byName);
  const publishers = booksInOrder(books, locale)
    .map(({ publisher }) => publisher)
    .filter((publisher) => publisher !== null)
    .map(spelled);
  return [...new Set(publishers)].sort(byName);
}

export function authorsOf(books: Book[], locale: string): string[] {
  const spelled = firstSpelling(nameOrder(locale));
  return authorsInOrder(
    booksInOrder(books, locale)
      .flatMap(({ authors }) => authors)
      .map(({ name, role }) => ({ name: spelled(name), role })),
    locale,
  )
    .filter(({ roles }) => roles.some((role) => role !== "TRANSLATOR"))
    .map(({ name }) => name);
}

function firstSpelling(
  byName: (a: string, b: string) => number,
): (name: string) => string {
  const spellings: string[] = [];
  return (name) => {
    const spelling = spellings.find((each) => byName(each, name) === 0);
    if (spelling !== undefined) {
      return spelling;
    }
    spellings.push(name);
    return name;
  };
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
