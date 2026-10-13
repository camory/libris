import { describe, expect, it } from "vitest";
import type { Bookshelf } from "./Bookshelf";
import { membershipsInOrder, type Membership } from "./Membership";

describe("membershipsInOrder", () => {
  it("puts the reader's default first", () => {
    expect(
      membershipsInOrder(
        [ownerOf(etagereDuSalon), ownerOf(salon)],
        salon.id,
        "fr",
      ),
    ).toEqual([ownerOf(salon), ownerOf(etagereDuSalon)]);
  });

  it("then the others by name, ignoring case and accents", () => {
    expect(
      membershipsInOrder(
        [
          ownerOf(salon),
          viewerOf(mangas),
          ownerOf(etagereDuSalon),
          ownerOf(bibliothequeDeLea),
        ],
        bibliothequeDeLea.id,
        "fr",
      ),
    ).toEqual([
      ownerOf(bibliothequeDeLea),
      ownerOf(etagereDuSalon),
      viewerOf(mangas),
      ownerOf(salon),
    ]);
  });
});

function ownerOf(bookshelf: Bookshelf): Membership {
  return { bookshelf, role: "OWNER" };
}

function viewerOf(bookshelf: Bookshelf): Membership {
  return { bookshelf, role: "VIEWER" };
}

const bibliothequeDeLea: Bookshelf = {
  id: "0b1e2d3c-4f5a-4b6c-8d7e-9f0a1b2c3d4e",
  name: "Bibliothèque de Léa",
};

const etagereDuSalon: Bookshelf = {
  id: "2d3e4f5a-6b7c-4d8e-9f0a-1b2c3d4e5f60",
  name: "Étagère du salon",
};

const salon: Bookshelf = {
  id: "1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f",
  name: "Salon",
};

const mangas: Bookshelf = {
  id: "3e4f5a6b-7c8d-4e9f-8a1b-2c3d4e5f6a71",
  name: "mangas",
};
