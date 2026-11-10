import { describe, expect, inject, it } from "vitest";
import type { EditionDetails } from "../../application/EditionsApi";
import { aBook } from "../../fixture/EditionShapes";
import { FetchEditionsApi } from "./FetchEditionsApi";

const editOnePiece2: EditionDetails = {
  kind: "MANGA",
  title: "Aux prises avec Baggy et ses hommes",
  subtitle: null,
  authors: [
    { name: "Eiichirō Oda", role: "WRITER" },
    { name: "Eiichirō Oda", role: "ARTIST" },
  ],
  series: { name: "One Piece", volumeNumber: 2 },
  collection: "Shonen manga",
  publisher: "Glénat",
  publicationYear: 2013,
  language: "fr",
  pageCount: 208,
  summary: "Luffy et Zoro affrontent le pirate Baggy le clown.",
};

describe("FetchEditionsApi", () => {
  it("saves the edition", async () => {
    // Given
    const api = new FetchEditionsApi(inject("mockBaseUrl"));

    // When
    const answer = await api.save(
      "3c4d5e6f-7a8b-4c9d-8e0f-1a2b3c4d5e6f",
      editOnePiece2,
    );

    // Then
    expect(answer).toEqual({ outcome: "saved", book: aBook });
  });

  it("answers a problem when no edition has the id", async () => {
    // Given
    const api = new FetchEditionsApi(inject("mockBaseUrl"));

    // When
    const answer = await api.save(
      "2a3b4c5d-6e7f-4a8b-9c0d-1e2f3a4b5c6d",
      editOnePiece2,
    );

    // Then
    expect(answer).toEqual({ outcome: "problem", type: "/problems/not-found" });
  });
});
