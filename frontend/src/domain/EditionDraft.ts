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

function text(value: string) {
  const trimmed = value.trim();
  return trimmed === "" ? null : trimmed;
}

function number(value: string) {
  const digits = text(value);
  return digits === null ? null : Number(digits);
}
