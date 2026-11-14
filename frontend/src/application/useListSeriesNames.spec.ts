import { describe, expect, it } from "vitest";
import { FakeSeriesApi } from "../fixture/FakeSeriesApi";
import { useListSeriesNames } from "./useListSeriesNames";

describe("useListSeriesNames", () => {
  it("holds the names Libris answers", async () => {
    // Given
    const { names, list } = useListSeriesNames(
      new FakeSeriesApi([], undefined, ["One piece", "Astérix"]),
    );
    const before = names.value;

    // When
    await list();

    // Then
    expect(before).toBeNull();
    expect(names.value).toEqual(["One piece", "Astérix"]);
  });

  it("holds no names when Libris does not answer", async () => {
    // Given
    const { names, list } = useListSeriesNames(
      new FakeSeriesApi([], undefined, new TypeError("Failed to fetch")),
    );

    // When
    const listed = list();

    // Then
    await expect(listed).resolves.toBeUndefined();
    expect(names.value).toBeNull();
  });

  it("holds the latest ask's names when an earlier answer lands after it", async () => {
    // Given
    const first = later();
    const second = later();
    const { names, list } = useListSeriesNames(
      new LibrisAnsweringInTurn([first.names, second.names]),
    );
    const earlier = list();
    const latest = list();

    // When
    second.arrives(["One piece", "Astérix"]);
    await latest;
    first.arrives(["One piece"]);
    await earlier;

    // Then
    expect(names.value).toEqual(["One piece", "Astérix"]);
  });
});

class LibrisAnsweringInTurn extends FakeSeriesApi {
  constructor(private readonly inTurn: Promise<string[]>[]) {
    super([]);
  }

  override names(): Promise<string[]> {
    return this.inTurn.shift()!;
  }
}

function later() {
  let arrives: (names: string[]) => void = () => {};
  let fails: (error: Error) => void = () => {};
  const names = new Promise<string[]>((resolve, reject) => {
    arrives = resolve;
    fails = reject;
  });
  return { names, arrives, fails };
}
