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
});
