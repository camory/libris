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
  const text = folded(typed, locale);
  return names.flatMap((name) => {
    const start = folded(name, locale).indexOf(text);
    if (start < 0) return [];
    const end = start + text.length;
    return [
      {
        before: name.slice(0, start),
        matched: name.slice(start, end),
        after: name.slice(end),
      },
    ];
  });
}

function folded(text: string, locale: string) {
  return text
    .normalize("NFD")
    .replace(/\p{M}/gu, "")
    .toLocaleLowerCase(locale);
}
