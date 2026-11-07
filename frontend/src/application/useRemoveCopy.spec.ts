import { flushPromises } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import type { CopiesApi, RemoveAnswer } from "./CopiesApi";
import { FakeCopiesApi } from "../fixture/FakeCopiesApi";
import { useRemoveCopy } from "./useRemoveCopy";

const copyId = "9c4a5f6e-7d8c-4b92-9ea3-4f5a6b7c8d94";

const nothingAfter = () => Promise.resolve();

describe("useRemoveCopy", () => {
  it("removes the copy it is given", async () => {
    // Given
    const copiesApi = new FakeCopiesApi({ outcome: "removed" });
    const { state, remove } = useRemoveCopy(copiesApi, nothingAfter);

    // When
    await remove(copyId);

    // Then
    expect(copiesApi.asked).toEqual([copyId]);
    expect(state.value).toEqual({ status: "removed" });
  });

  it("is removing while Libris has not answered", () => {
    // Given
    const { state, remove } = useRemoveCopy(
      new LibrisNotAnswering(),
      nothingAfter,
    );

    // When
    void remove(copyId);

    // Then
    expect(state.value).toEqual({ status: "removing" });
  });

  it("is removing until what follows the removal is done", async () => {
    // Given
    const { state, remove } = useRemoveCopy(
      new FakeCopiesApi({ outcome: "removed" }),
      () => new Promise(() => {}),
    );

    // When
    void remove(copyId);
    await flushPromises();

    // Then
    expect(state.value).toEqual({ status: "removing" });
  });

  it("is unavailable when Libris does not answer", async () => {
    // Given
    const { state, remove } = useRemoveCopy(
      new FakeCopiesApi(new TypeError("Failed to fetch")),
      nothingAfter,
    );

    // When
    await remove(copyId);

    // Then
    expect(state.value).toEqual({ status: "unavailable" });
  });

  it("is unavailable when Libris refuses the id", async () => {
    // Given
    const { state, remove } = useRemoveCopy(
      new FakeCopiesApi({ outcome: "problem", type: "/problems/validation" }),
      nothingAfter,
    );

    // When
    await remove(copyId);

    // Then
    expect(state.value).toEqual({ status: "unavailable" });
  });

  it("takes a copy Libris finds no more as removed", async () => {
    // Given
    const after = new RecordingAfterRemoval();
    const { state, remove } = useRemoveCopy(
      new FakeCopiesApi({ outcome: "problem", type: "/problems/not-found" }),
      after.run,
    );

    // When
    await remove(copyId);

    // Then
    expect(state.value).toEqual({ status: "removed" });
    expect(after.calls).toBe(1);
  });
});

class LibrisNotAnswering implements CopiesApi {
  remove(): Promise<RemoveAnswer> {
    return new Promise(() => {});
  }
}

class RecordingAfterRemoval {
  calls = 0;

  readonly run = () => {
    this.calls += 1;
    return Promise.resolve();
  };
}
