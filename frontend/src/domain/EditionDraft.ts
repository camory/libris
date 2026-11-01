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
  return {
    isbn13,
    kind: draft.kind,
    title: draft.title,
    subtitle: text(draft.subtitle),
    authors,
    series:
      text(draft.series) === null
        ? null
        : { name: draft.series, volumeNumber: number(draft.volume) },
    collection: text(draft.collection),
    publisher: text(draft.publisher),
    publicationYear: number(draft.year),
    language: language === null ? null : languageCode(language, locale),
    pageCount: number(draft.pages),
    summary: text(draft.summary),
  };
}

function text(value: string) {
  return value === "" ? null : value;
}

function number(value: string) {
  return value === "" ? null : Number(value);
}
