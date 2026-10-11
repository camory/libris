import { describe, expect, it } from "vitest";
import type { Bookshelf } from "./Bookshelf";
import { bookshelvesHolding } from "./Copy";

describe("bookshelvesHolding", () => {
  it("answers a bookshelf once, with its count of copies", () => {
    // Given
    const lea: Bookshelf = {
      id: "0c1d2e3f-4a5b-4c6d-8e7f-8091a2b3c4d5",
      name: "Bibliothèque de Léa",
    };

    // When
    const bookshelves = bookshelvesHolding(
      [
        { id: "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d", bookshelf: lea },
        { id: "2b3c4d5e-6f7a-4b8c-9d0e-1f2a3b4c5d6e", bookshelf: lea },
      ],
      "fr",
    );

    // Then
    expect(bookshelves).toEqual([{ bookshelf: lea, count: 2 }]);
  });

  it("sorts the bookshelves by name, ignoring case and accents", () => {
    // Given
    const salon: Bookshelf = {
      id: "3c4d5e6f-7a8b-4c9d-8e0f-1a2b3c4d5e6f",
      name: "Salon",
    };
    const chambre: Bookshelf = {
      id: "4d5e6f7a-8b9c-4d0e-9f1a-2b3c4d5e6f7a",
      name: "chambre",
    };
    const etagere: Bookshelf = {
      id: "5e6f7a8b-9c0d-4e1f-8a2b-3c4d5e6f7a8b",
      name: "Étagère",
    };

    // When
    const bookshelves = bookshelvesHolding(
      [
        { id: "6f7a8b9c-0d1e-4f2a-9b3c-4d5e6f7a8b9c", bookshelf: salon },
        { id: "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d", bookshelf: chambre },
        { id: "8b9c0d1e-2f3a-4b4c-9d5e-6f7a8b9c0d1e", bookshelf: etagere },
      ],
      "fr",
    );

    // Then
    expect(bookshelves).toEqual([
      { bookshelf: chambre, count: 1 },
      { bookshelf: etagere, count: 1 },
      { bookshelf: salon, count: 1 },
    ]);
  });
});
