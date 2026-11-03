import { languageCode } from "./Language";
import type { Kind, SourceAuthor, SourceEdition } from "./SourceEdition";

export interface EditionDraft {
  kind: Kind;
  title: string;
  subtitle: string;
  series: string;
  volume: string;
  collection: string;
  publisher: string;
  year: string;
  language: string;
  pages: string;
  summary: string;
}

export function editionOf(
  isbn13: string,
  authors: SourceAuthor[],
  draft: EditionDraft,
  locale: string,
): SourceEdition {
  const language = text(draft.language);
  const series = text(draft.series);
  return {
    isbn13,
    kind: draft.kind,
    title: draft.title.trim(),
    subtitle: text(draft.subtitle),
    authors,
    series:
      series === null
        ? null
        : { name: series, volumeNumber: number(draft.volume) },
    collection: text(draft.collection),
    publisher: text(draft.publisher),
    publicationYear: number(draft.year),
    language: language === null ? null : languageCode(language, locale),
    pageCount: number(draft.pages),
    summary: text(draft.summary),
  };
}

export type Refusal =
  "missing" | "invalidNumber" | "unknownLanguage" | "withoutSeries";

export type Refusals = Partial<
  Record<Exclude<keyof EditionDraft, "kind" | "summary">, Refusal>
>;

export function refusalsOf(draft: EditionDraft, locale: string): Refusals {
  const refusals: Refusals = {};
  if (text(draft.title) === null) refusals.title = "missing";
  if (text(draft.volume) !== null && text(draft.series) === null) {
    refusals.volume = "withoutSeries";
  }
  for (const field of ["volume", "year", "pages"] as const) {
    const digits = text(draft[field]);
    if (digits !== null && !isWholeNumber(digits)) {
      refusals[field] = "invalidNumber";
    }
  }
  const language = text(draft.language);
  if (language !== null && languageCode(language, locale) === null) {
    refusals.language = "unknownLanguage";
  }
  return refusals;
}

function isWholeNumber(value: string) {
  return /^\d+$/.test(value) && Number(value) >= 1;
}

function text(value: string) {
  const trimmed = value.trim();
  return trimmed === "" ? null : trimmed;
}

function number(value: string) {
  const digits = text(value);
  return digits === null ? null : Number(digits);
}
