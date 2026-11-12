import { describe, expect, it } from "vitest";
import { asterixLeGaulois, romanceDawn } from "../fixture/Books";
import { FakeSearchApi } from "../fixture/FakeSearchApi";
import { useSearchCatalogue } from "./useSearchCatalogue";

describe("useSearchCatalogue", () => {
  it("holds the books of the first page", async () => {
    // Given
    const { state, search } = useSearchCatalogue(
      new FakeSearchApi({
        goscinny: [
          { books: [romanceDawn, asterixLeGaulois], next: null, near: false },
        ],
      }),
    );

    // When
    await search("goscinny");

    // Then
    expect(state.value).toEqual({
      status: "found",
      books: [romanceDawn, asterixLeGaulois],
    });
  });
});
