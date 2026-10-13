import { describe, expect, it } from "vitest";
import type { KnownReader } from "./KnownReader";
import { membersInOrder, nonMembersInOrder, type Member } from "./Member";

describe("membersInOrder", () => {
  it("puts the owners first", () => {
    expect(membersInOrder([viewer(hugo), owner(marc)], "fr")).toEqual([
      owner(marc),
      viewer(hugo),
    ]);
  });

  it("then each role by display name, ignoring case and accents", () => {
    expect(
      membersInOrder(
        [owner(marc), viewer(hugoInLowerCase), owner(elodie), viewer(anais)],
        "fr",
      ),
    ).toEqual([
      owner(elodie),
      owner(marc),
      viewer(anais),
      viewer(hugoInLowerCase),
    ]);
  });
});

describe("nonMembersInOrder", () => {
  it("offers the readers who are not members", () => {
    expect(
      nonMembersInOrder([lea, marc, hugo], [owner(lea), viewer(marc)], "fr"),
    ).toEqual([hugo]);
  });
});

function owner(reader: KnownReader): Member {
  return { reader, role: "OWNER" };
}

function viewer(reader: KnownReader): Member {
  return { reader, role: "VIEWER" };
}

const lea: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000002",
  displayName: "Léa",
};

const hugo: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000004",
  displayName: "Hugo",
};

const marc: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000005",
  displayName: "Marc",
};

const hugoInLowerCase: KnownReader = { ...hugo, displayName: "hugo" };

const elodie: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000003",
  displayName: "Élodie",
};

const anais: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000006",
  displayName: "Anaïs",
};
