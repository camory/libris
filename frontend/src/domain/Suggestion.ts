import { nameOrder } from "./nameOrder";

export interface Suggestion {
  before: string;
  matched: string;
  after: string;
}

export function suggestionsFor(
  typed: string,
  names: string[],
  locale: string,
): Suggestion[] {
  const text = folded(typed.trim(), locale);
  if (text === "") return [];
  return [...names]
    .sort(nameOrder(locale))
    .flatMap((name) => {
      const suggestion = suggestionOf(name, text, locale);
      return suggestion ? [suggestion] : [];
    })
    .slice(0, 5);
}

function suggestionOf(
  name: string,
  text: string,
  locale: string,
): Suggestion | null {
  const characters = Array.from(name);
  const folds = characters.map((character) => folded(character, locale));
  const start = folds.join("").indexOf(text);
  if (start < 0) return null;
  const ends = folds.map(
    (fold, index) => folds.slice(0, index).join("").length + fold.length,
  );
  const first = ends.findIndex((end) => end > start);
  const next = ends.findIndex(
    (end, index) =>
      index > first && folds[index] !== "" && end > start + text.length,
  );
  const last = next < 0 ? characters.length : next;
  return {
    before: characters.slice(0, first).join(""),
    matched: characters.slice(first, last).join(""),
    after: characters.slice(last).join(""),
  };
}

function folded(text: string, locale: string) {
  return text.normalize("NFD").replace(/\p{M}/gu, "").toLocaleLowerCase(locale);
}
