import { describe, expect, it } from "vitest";
import { FakeCopiesApi } from "../fixture/FakeCopiesApi";
import { useRemoveCopy } from "./useRemoveCopy";

const copyId = "9c4a5f6e-7d8c-4b92-9ea3-4f5a6b7c8d94";

describe("useRemoveCopy", () => {
  it("removes the copy it is given", async () => {
    // Given
    const copiesApi = new FakeCopiesApi({ outcome: "removed" });
    const { state, remove } = useRemoveCopy(copiesApi);

    // When
    await remove(copyId);

    // Then
    expect(copiesApi.asked).toEqual([copyId]);
    expect(state.value).toEqual({ status: "removed" });
  });
});
