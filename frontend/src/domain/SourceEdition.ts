export type AuthorRole = "WRITER" | "ARTIST" | "COLOURIST" | "TRANSLATOR";

export type Kind = "BOOK" | "BD" | "MANGA";

export interface SourceAuthor {
  name: string;
  role: AuthorRole;
}

export interface SourceSeries {
  name: string;
  volumeNumber: number | null;
}

export interface SourceEdition {
  isbn13: string;
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
}
