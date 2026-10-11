import { describe, expect, it } from "vitest";
import { authorsInOrder } from "./SourceEdition";

describe("authorsInOrder", () => {
  it("answers an author listed under several roles once, each role once, in the order scénario, dessin, couleurs, traduction", () => {
    // Given
    const name = "Eiichirō Oda";

    // When
    const authors = authorsInOrder(
      [
        { name, role: "COLOURIST" },
        { name, role: "WRITER" },
        { name, role: "TRANSLATOR" },
        { name, role: "ARTIST" },
        { name, role: "WRITER" },
      ],
      "fr",
    );

    // Then
    expect(authors).toEqual([
      { name, roles: ["WRITER", "ARTIST", "COLOURIST", "TRANSLATOR"] },
    ]);
  });

  it("sorts the authors by their first role", () => {
    // When
    const authors = authorsInOrder(
      [
        { name: "Boichi", role: "ARTIST" },
        { name: "Eiichirō Oda", role: "ARTIST" },
        { name: "Eiichirō Oda", role: "WRITER" },
      ],
      "fr",
    );

    // Then
    expect(authors).toEqual([
      { name: "Eiichirō Oda", roles: ["WRITER", "ARTIST"] },
      { name: "Boichi", roles: ["ARTIST"] },
    ]);
  });

  it("then sorts them by name, ignoring case and accents", () => {
    // When
    const authors = authorsInOrder(
      [
        { name: "Fabien Vehlmann", role: "WRITER" },
        { name: "emmanuel Guibert", role: "WRITER" },
        { name: "Émile Bravo", role: "WRITER" },
      ],
      "fr",
    );

    // Then
    expect(authors).toEqual([
      { name: "Émile Bravo", roles: ["WRITER"] },
      { name: "emmanuel Guibert", roles: ["WRITER"] },
      { name: "Fabien Vehlmann", roles: ["WRITER"] },
    ]);
  });
});
