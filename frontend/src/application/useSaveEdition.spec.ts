import { describe, expect, it } from "vitest";
import { asterixLeGaulois } from "../fixture/Books";
import { FakeEditionsApi } from "../fixture/FakeEditionsApi";
import { onePiece2 } from "../fixture/SourceEditions";
import { useSaveEdition } from "./useSaveEdition";

const id = "4d5e6f7a-8b9c-4d0e-9f1a-2b3c4d5e6f7a";

describe("useSaveEdition", () => {
  it("saves the edition under its id, without its ISBN", async () => {
    // Given
    const editionsApi = new FakeEditionsApi({
      outcome: "saved",
      book: asterixLeGaulois,
    });
    const { save } = useSaveEdition(editionsApi);

    // When
    await save(id, onePiece2);

    // Then
    expect(editionsApi.asked).toEqual([
      {
        id,
        edition: {
          kind: "MANGA",
          title: "Aux prises avec Baggy et ses hommes",
          subtitle: null,
          authors: [
            { name: "Eiichirō Oda", role: "WRITER" },
            { name: "Eiichirō Oda", role: "ARTIST" },
          ],
          series: { name: "One piece", volumeNumber: 2 },
          collection: "Shonen manga",
          publisher: "Glénat",
          publicationYear: 2013,
          language: "fr",
          pageCount: 208,
          summary: null,
        },
      },
    ]);
  });
});
