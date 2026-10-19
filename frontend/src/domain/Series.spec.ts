import { describe, expect, it } from "vitest";
import { seriesInOrder, type Series } from "./Series";

describe("seriesInOrder", () => {
  it("sorts the series by name, ignoring case and accents", () => {
    expect(
      seriesInOrder([onePiece, emileEtMargot, dragonQuest, asterix], "fr"),
    ).toEqual([asterix, dragonQuest, emileEtMargot, onePiece]);
  });
});

const onePiece: Series = {
  id: "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
  name: "One piece",
  volumeCount: 3,
  coverUrl: null,
  kind: "MANGA",
};

const emileEtMargot: Series = {
  id: "2b3c4d5e-6f7a-4b8c-9d0e-1f2a3b4c5d6e",
  name: "Émile et Margot",
  volumeCount: 2,
  coverUrl: null,
  kind: "BD",
};

const dragonQuest: Series = {
  id: "3c4d5e6f-7a8b-4c9d-8e0f-2a3b4c5d6e7f",
  name: "dragon quest",
  volumeCount: 2,
  coverUrl: null,
  kind: "MANGA",
};

const asterix: Series = {
  id: "4d5e6f7a-8b9c-4d0e-9f1a-3b4c5d6e7f80",
  name: "Astérix",
  volumeCount: 6,
  coverUrl: null,
  kind: "BD",
};
