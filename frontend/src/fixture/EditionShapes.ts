import { expect } from "vitest";

export const aStringOrNull = expect.toSatisfy(
  (value: unknown) => value === null || typeof value === "string",
  "a string or null",
);

export const aNumberOrNull = expect.toSatisfy(
  (value: unknown) => value === null || typeof value === "number",
  "a number or null",
);

export const aKind = expect.toSatisfy(
  (value: unknown) => ["BOOK", "BD", "MANGA"].includes(value as string),
  "one of the kinds the API answers",
);

export const authorsWithNameAndRole = expect.toSatisfy(
  (value: unknown) =>
    Array.isArray(value) &&
    value.every(
      (author) =>
        typeof author.name === "string" &&
        ["WRITER", "ARTIST", "COLOURIST", "TRANSLATOR"].includes(author.role),
    ),
  "authors with a name and a role",
);

export const candidatesWithSourceAndUrl = expect.toSatisfy(
  (value: unknown) =>
    Array.isArray(value) &&
    value.every(
      (candidate) =>
        typeof candidate.source === "string" &&
        typeof candidate.url === "string",
    ),
  "cover candidates with a source and a url",
);

export const aSeriesOrNull = expect.toSatisfy((value: unknown) => {
  if (value === null) {
    return true;
  }
  const series = value as { name: unknown; volumeNumber: unknown };
  return (
    typeof series.name === "string" &&
    (series.volumeNumber === null || typeof series.volumeNumber === "number")
  );
}, "a series or null");
