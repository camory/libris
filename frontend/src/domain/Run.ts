import type { Book } from "./Book";

export interface Run {
  first: number;
  last: number;
}

export function runsOf(books: Book[]): { owned: Run[]; missing: Run[] } {
  const tomes = [
    ...new Set(books.flatMap((book) => book.series?.volumeNumber ?? [])),
  ].sort((a, b) => a - b);
  const highest = tomes.at(-1) ?? 0;
  const missing: Run[] = [];
  for (let tome = 1; tome <= highest; tome++) {
    if (!tomes.includes(tome)) {
      missing.push({ first: tome, last: tome });
    }
  }
  return {
    owned: tomes.map((tome) => ({ first: tome, last: tome })),
    missing,
  };
}
