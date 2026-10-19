import type { Book } from "./Book";

export interface Run {
  first: number;
  last: number;
}

export function runsOf(books: Book[]): { owned: Run[]; missing: Run[] } {
  const owned = new Set(
    books.flatMap((book) => book.series?.volumeNumber ?? []),
  );
  const highest = Math.max(0, ...owned);
  const upToHighest = Array.from({ length: highest }, (_, i) => i + 1);
  return {
    owned: runsAmong(upToHighest.filter((tome) => owned.has(tome))),
    missing: runsAmong(upToHighest.filter((tome) => !owned.has(tome))),
  };
}

function runsAmong(tomesInOrder: number[]): Run[] {
  const runs: Run[] = [];
  for (const tome of tomesInOrder) {
    const last = runs.at(-1);
    if (last?.last === tome - 1) {
      last.last = tome;
    } else {
      runs.push({ first: tome, last: tome });
    }
  }
  return runs;
}
