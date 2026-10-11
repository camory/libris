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
}

export interface Author {
  name: string;
  roles: AuthorRole[];
}

export function authorsInOrder(
  authors: SourceAuthor[],
  locale: string,
): Author[] {
  const played = new Map<string, AuthorRole[]>();
  for (const { name, role } of authors) {
    const roles = played.get(name) ?? [];
    if (!roles.includes(role)) {
      roles.push(role);
    }
    played.set(name, roles);
  }
  return [...played].map(([name, roles]) => ({
    name,
    roles: roles.sort((a, b) => ROLES.indexOf(a) - ROLES.indexOf(b)),
  }));
}

const ROLES: AuthorRole[] = ["WRITER", "ARTIST", "COLOURIST", "TRANSLATOR"];
