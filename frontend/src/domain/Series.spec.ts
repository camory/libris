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
  name: "One piece",
  volumeCount: 3,
  coverUrl: null,
  kind: "MANGA",
};

const emileEtMargot: Series = {
  name: "Émile et Margot",
  volumeCount: 2,
  coverUrl: null,
  kind: "BD",
};

const dragonQuest: Series = {
  name: "dragon quest",
  volumeCount: 2,
  coverUrl: null,
  kind: "MANGA",
};

const asterix: Series = {
  name: "Astérix",
  volumeCount: 6,
  coverUrl: null,
  kind: "BD",
};
