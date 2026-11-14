import { describe, expect, it } from "vitest";
import { FakeAuthorsApi } from "../fixture/FakeAuthorsApi";
import type { AuthorsApi } from "./AuthorsApi";
import { useListAuthorNames } from "./useListAuthorNames";

describe("useListAuthorNames", () => {
  it("holds the names Libris answers", async () => {
    // Given
    const { names, list } = useListAuthorNames(
      new FakeAuthorsApi(["René Goscinny", "Hergé"]),
    );
    const before = names.value;

    // When
    await list();

    // Then
    expect(before).toBeNull();
    expect(names.value).toEqual(["René Goscinny", "Hergé"]);
  });

  it("holds no names when Libris does not answer", async () => {
    // Given
    const { names, list } = useListAuthorNames(
      new FakeAuthorsApi(new TypeError("Failed to fetch")),
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
    const { names, list } = useListAuthorNames(
      new LibrisAnsweringInTurn([first.names, second.names]),
    );
    const earlier = list();
    const latest = list();

    // When
    second.arrives(["René Goscinny", "Hergé"]);
    await latest;
    first.arrives(["Hergé"]);
    await earlier;

    // Then
    expect(names.value).toEqual(["René Goscinny", "Hergé"]);
  });
});

class LibrisAnsweringInTurn implements AuthorsApi {
  constructor(private readonly inTurn: Promise<string[]>[]) {}

  names(): Promise<string[]> {
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
