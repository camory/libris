import {
  fireEvent,
  queries,
  within,
  waitFor,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, describe, expect, it } from "vitest";
import type { App } from "vue";
import type { BookshelvesApi } from "../application/BookshelvesApi";
import type { BookshelfApi } from "../application/BookshelfApi";
import type { IsbnApi } from "../application/IsbnApi";
import type { ReadersApi } from "../application/ReadersApi";
import { createLibrisApp } from "../createLibrisApp";
import type { Bookshelf } from "../domain/Bookshelf";
import type { Copy } from "../domain/Copy";
import type { KnownReader } from "../domain/KnownReader";
import type { Member } from "../domain/Member";
import type { Membership, MembershipRole } from "../domain/Membership";
import type { SourceEdition } from "../domain/SourceEdition";
import { FakeAppUpdate } from "../fixture/FakeAppUpdate";
import { FakeBarcodeScanner } from "../fixture/FakeBarcodeScanner";
import { FakeBookshelfApi } from "../fixture/FakeBookshelfApi";
import { FakeBookshelvesApi } from "../fixture/FakeBookshelvesApi";
import { FakeCatalogueApi } from "../fixture/FakeCatalogueApi";
import { FakeIsbnApi } from "../fixture/FakeIsbnApi";
import { FakeMeApi } from "../fixture/FakeMeApi";
import { FakeReadersApi } from "../fixture/FakeReadersApi";
import { lea } from "../fixture/Readers";
import { onePiece1 } from "../fixture/SourceEditions";

type Screen = BoundFunctions<typeof queries>;

const etagereDuSalon: Bookshelf = {
  id: "2d3e4f5a-6b7c-4d8e-9f0a-1b2c3d4e5f60",
  name: "Étagère du salon",
};

const bibliothequeFamiliale: Bookshelf = {
  id: "4f5a6b7c-8d9e-4f0a-9b2c-3d4e5f6a7b82",
  name: "Bibliothèque familiale",
};

const salon: Bookshelf = {
  id: "1c2f3e4d-5a6b-4c7d-9e8f-0a1b2c3d4e5f",
  name: "Salon",
};

const mangas: Bookshelf = {
  id: "3e4f5a6b-7c8d-4e9f-8a1b-2c3d4e5f6a71",
  name: "Mangas",
};

const leaToOthers: KnownReader = { id: lea.id, displayName: lea.displayName };

const elodie: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000003",
  displayName: "Élodie",
};

const hugo: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000004",
  displayName: "Hugo",
};

const marc: KnownReader = {
  id: "0199c0de-1000-7000-8000-000000000005",
  displayName: "Marc",
};

describe("Bookshelves", () => {
  const host = document.createElement("div");
  let app: App;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
  });

  it("S1 The reader's bookshelves", async () => {
    // Given
    const libris = leaBelongsTo(
      viewerOf(mangas),
      ownerOf(etagereDuSalon),
      ownerOf(lea.defaultBookshelf),
    );

    // When
    const screen = open("/", libris);

    // Then
    await screen.findByText("Mangas");
    expect(rows(screen)).toEqual([
      "Bibliothèque de Léa propriétaire · par défaut",
      "Étagère du salon propriétaire",
      "Mangas invité",
    ]);
  });

  it("S2 A reader creates a bookshelf", async () => {
    // Given
    const screen = open(
      "/",
      librisCreates(bibliothequeFamiliale, ownerOf(lea.defaultBookshelf)),
    );
    await screen.findByText("Bibliothèque de Léa");

    // When
    await create(screen, "Bibliothèque familiale");

    // Then
    await screen.findByText("Bibliothèque familiale");
    expect(rows(screen)).toEqual([
      "Bibliothèque de Léa propriétaire · par défaut",
      "Bibliothèque familiale propriétaire",
    ]);
  });

  it("S3 A name Libris refuses, a name taken", async () => {
    // Given
    const screen = open(
      "/",
      librisRefusesTheName(ownerOf(lea.defaultBookshelf), viewerOf(mangas)),
    );
    await screen.findByText("Mangas");

    // When
    await create(screen, "mangas");

    // Then
    await screen.findByText("Vous avez déjà une bibliothèque de ce nom.");
    expect(nameField(screen).value).toBe("mangas");
    expect(rows(screen)).toEqual([
      "Bibliothèque de Léa propriétaire · par défaut",
      "Mangas invité",
      "Vous avez déjà une bibliothèque de ce nom.",
    ]);
  });

  it("S4 A member opens a bookshelf", async () => {
    // Given
    const libris = librisHolds(
      salon,
      viewer(leaToOthers),
      owner(marc),
      owner(elodie),
    );

    // When
    const screen = open(`/bibliotheques/${salon.id}`, libris);

    // Then
    await screen.findByRole("heading", { name: "Salon" });
    expect(members(screen)).toEqual([
      "Vous · invité",
      "Élodie · propriétaire",
      "Marc · propriétaire",
    ]);
  });

  it("S4 A member opens a bookshelf, a reader who does not belong to it", async () => {
    // Given
    const libris = new FakeBookshelvesApi({
      memberships: [ownerOf(lea.defaultBookshelf)],
      find: { outcome: "problem", type: "/problems/not-found" },
    });

    // When
    const screen = open(`/bibliotheques/${salon.id}`, libris);

    // Then
    await screen.findByText("Bibliothèque introuvable.");
    expect(screen.queryByRole("heading", { name: "Salon" })).toBeNull();
    expect(screen.queryByRole("list", { name: "Membres" })).toBeNull();
  });

  it.each([
    {
      case: "added as owner",
      role: "OWNER",
      then: ["Vous · propriétaire", "Hugo · propriétaire", "Marc · invité"],
    },
    {
      case: "added as viewer",
      role: "VIEWER",
      then: ["Vous · propriétaire", "Hugo · invité", "Marc · invité"],
    },
  ] as const)("S5 An owner adds a member, $case", async ({ role, then }) => {
    // Given
    const screen = open(
      `/bibliotheques/${salon.id}`,
      librisAdds(hugo, salon, owner(leaToOthers), viewer(marc)),
      { readers: new FakeReadersApi([leaToOthers, marc, hugo]) },
    );
    await screen.findByRole("heading", { name: "Salon" });

    // When
    await add(screen, hugo, role);

    // Then
    await waitFor(() => expect(members(screen)).toEqual(then));
  });

  it.skip("S7 A reader chooses their default", async () => {
    // Given
    const me = new FakeMeApi(lea);
    const screen = open(
      "/",
      new FakeBookshelvesApi({
        memberships: [ownerOf(lea.defaultBookshelf), ownerOf(salon)],
        defaultChosen: (bookshelf) => me.defaultIs(bookshelf),
      }),
      {
        me,
        isbn: aSourceKnows(onePiece1),
        add: librisAddsACopyOn(lea.defaultBookshelf, salon),
      },
    );
    await screen.findByText("Salon");

    // When
    await fireEvent.click(
      within(row(screen, "Salon")).getByRole("button", {
        name: "Choisir comme bibliothèque par défaut",
      }),
    );

    // Then
    await waitFor(() =>
      expect(rows(screen)).toEqual([
        "Salon propriétaire · par défaut",
        "Bibliothèque de Léa propriétaire",
      ]),
    );
    await fireEvent.click(screen.getByRole("link", { name: "Ajouter" }));
    await ask(screen, "9782723488525");
    await screen.findByText("Romance dawn");
    await fireEvent.click(
      screen.getByRole("button", { name: "Ajouter à ma bibliothèque" }),
    );
    await screen.findByText("Dans Salon");
  });

  it.skip("S8 A bookshelf the reader cannot make their default", async () => {
    // Given
    const libris = leaBelongsTo(
      ownerOf(lea.defaultBookshelf),
      ownerOf(salon),
      viewerOf(mangas),
    );

    // When
    const screen = open("/", libris);

    // Then
    await screen.findByText("Mangas");
    expect(within(row(screen, "Mangas")).queryByRole("button")).toBeNull();
    expect(
      within(row(screen, "Bibliothèque de Léa")).queryByRole("button"),
    ).toBeNull();
    expect(
      within(row(screen, "Salon")).getByRole("button", {
        name: "Choisir comme bibliothèque par défaut",
      }),
    ).toBeDefined();
  });

  it("S9 Libris unavailable, the home page's bookshelves", async () => {
    // Given
    const libris = new FakeBookshelvesApi({ memberships: noAnswer() });

    // When
    const screen = open("/", libris);

    // Then
    await screen.findByText(
      "Erreur lors du chargement, veuillez réessayer plus tard.",
    );
    expect(
      screen.queryByRole("list", { name: "Mes bibliothèques" }),
    ).toBeNull();
    expect(
      screen.queryByRole("button", { name: "Nouvelle bibliothèque" }),
    ).toBeNull();
  });

  it("S9 Libris unavailable, opening a bookshelf", async () => {
    // Given
    const libris = new FakeBookshelvesApi({
      memberships: [ownerOf(lea.defaultBookshelf)],
      find: noAnswer(),
    });

    // When
    const screen = open(`/bibliotheques/${salon.id}`, libris);

    // Then
    await screen.findByText(
      "Erreur lors du chargement, veuillez réessayer plus tard.",
    );
    expect(screen.queryByRole("heading", { name: "Salon" })).toBeNull();
    expect(screen.queryByRole("list", { name: "Membres" })).toBeNull();
  });

  it("S9 Libris unavailable, creating a bookshelf", async () => {
    // Given
    const screen = open(
      "/",
      new FakeBookshelvesApi({
        memberships: [ownerOf(lea.defaultBookshelf)],
        create: noAnswer(),
      }),
    );
    await screen.findByText("Bibliothèque de Léa");

    // When
    await create(screen, "Bibliothèque familiale");

    // Then
    await screen.findByText(
      "Erreur lors de la création, veuillez réessayer plus tard.",
    );
    expect(nameField(screen).value).toBe("Bibliothèque familiale");
    expect(rows(screen)).toEqual([
      "Bibliothèque de Léa propriétaire · par défaut",
      "Erreur lors de la création, veuillez réessayer plus tard.",
    ]);
  });

  it.skip("S9 Libris unavailable, adding a member", async () => {
    // Given
    const screen = open(
      `/bibliotheques/${salon.id}`,
      new FakeBookshelvesApi({
        memberships: [ownerOf(lea.defaultBookshelf)],
        find: {
          outcome: "found",
          bookshelf: salon,
          members: [owner(leaToOthers), viewer(marc)],
        },
        adds: noAnswer(),
      }),
      { readers: new FakeReadersApi([leaToOthers, marc, hugo]) },
    );
    await screen.findByRole("heading", { name: "Salon" });

    // When
    await add(screen, hugo, "OWNER");

    // Then
    await screen.findByText(
      "Erreur lors de l'ajout, veuillez réessayer plus tard.",
    );
    expect(readersToChoose(screen)).toEqual([
      "Hugo Erreur lors de l'ajout, veuillez réessayer plus tard.",
    ]);
    expect(
      screen.getByRole("button", { name: "Propriétaire", pressed: true }),
    ).toBeDefined();
    expect(members(screen)).toEqual(["Vous · propriétaire", "Marc · invité"]);
  });

  it.skip("S9 Libris unavailable, choosing the default", async () => {
    // Given
    const screen = open(
      "/",
      new FakeBookshelvesApi({
        memberships: [ownerOf(lea.defaultBookshelf), ownerOf(salon)],
        defaultChosen: noAnswer(),
      }),
    );
    await screen.findByText("Salon");

    // When
    await fireEvent.click(
      within(row(screen, "Salon")).getByRole("button", {
        name: "Choisir comme bibliothèque par défaut",
      }),
    );

    // Then
    await screen.findByText(
      "Erreur lors du changement, veuillez réessayer plus tard.",
    );
    expect(rows(screen)).toEqual([
      "Bibliothèque de Léa propriétaire · par défaut",
      "Salon propriétaire Erreur lors du changement, veuillez réessayer plus tard.",
    ]);
    expect(
      within(row(screen, "Salon")).getByRole("button", {
        name: "Choisir comme bibliothèque par défaut",
      }),
    ).toBeDefined();
  });

  function leaBelongsTo(...memberships: Membership[]): BookshelvesApi {
    return new FakeBookshelvesApi({ memberships });
  }

  function librisCreates(
    bookshelf: Bookshelf,
    ...memberships: Membership[]
  ): BookshelvesApi {
    return new FakeBookshelvesApi({
      memberships,
      create: { outcome: "created", bookshelf },
    });
  }

  function librisRefusesTheName(...memberships: Membership[]): BookshelvesApi {
    return new FakeBookshelvesApi({
      memberships,
      create: { outcome: "problem", type: "/problems/conflict" },
    });
  }

  function librisHolds(bookshelf: Bookshelf, ...members: Member[]) {
    return new FakeBookshelvesApi({
      memberships: [ownerOf(lea.defaultBookshelf)],
      find: { outcome: "found", bookshelf, members },
    });
  }

  function librisAdds(
    reader: KnownReader,
    bookshelf: Bookshelf,
    ...members: Member[]
  ) {
    return new FakeBookshelvesApi({
      memberships: [ownerOf(lea.defaultBookshelf)],
      find: { outcome: "found", bookshelf, members },
      adds: reader,
    });
  }

  function aSourceKnows(edition: SourceEdition): IsbnApi {
    return new FakeIsbnApi({
      outcome: "found",
      id: null,
      edition,
      copies: [],
      covers: [],
    });
  }

  function librisAddsACopyOn(...bookshelves: Bookshelf[]): BookshelfApi {
    return new FakeBookshelfApi((bookshelfId) => {
      const bookshelf = bookshelves.find(({ id }) => id === bookshelfId);
      return bookshelf === undefined
        ? { outcome: "problem", type: "/problems/not-found" }
        : { outcome: "added", copy: copyOn(bookshelf) };
    });
  }

  function copyOn(bookshelf: Bookshelf): Copy {
    return { id: "5e0c1b2a-3948-4d5e-8a6f-0b1c2d3e4f50", bookshelf };
  }

  function noAnswer() {
    return new TypeError("Failed to fetch");
  }

  function owner(reader: KnownReader): Member {
    return { reader, role: "OWNER" };
  }

  function viewer(reader: KnownReader): Member {
    return { reader, role: "VIEWER" };
  }

  function ownerOf(bookshelf: Bookshelf): Membership {
    return { bookshelf, role: "OWNER" };
  }

  function viewerOf(bookshelf: Bookshelf): Membership {
    return { bookshelf, role: "VIEWER" };
  }

  function open(
    path: string,
    bookshelves: BookshelvesApi,
    world: {
      readers?: ReadersApi;
      me?: FakeMeApi;
      isbn?: IsbnApi;
      add?: BookshelfApi;
    } = {},
  ) {
    window.history.replaceState(null, "", path);
    app = createLibrisApp(
      {
        meApi: world.me ?? new FakeMeApi(lea),
        isbnApi:
          world.isbn ??
          new FakeIsbnApi({ outcome: "problem", type: "/problems/not-found" }),
        bookshelfApi:
          world.add ??
          new FakeBookshelfApi(new Error("no add in this scenario")),
        barcodeScanner: new FakeBarcodeScanner(false),
        appUpdate: new FakeAppUpdate(),
        catalogueApi: new FakeCatalogueApi([]),
        bookshelvesApi: bookshelves,
        readersApi: world.readers ?? new FakeReadersApi([leaToOthers]),
      },
      "sha-abc1234",
    );
    app.mount(host);
    return within(host);
  }

  async function create(screen: Screen, name: string) {
    await fireEvent.click(
      screen.getByRole("button", { name: "Nouvelle bibliothèque" }),
    );
    await fireEvent.input(nameField(screen), { target: { value: name } });
    await fireEvent.click(screen.getByRole("button", { name: "Créer" }));
  }

  function nameField(screen: Screen) {
    return screen.getByPlaceholderText<HTMLInputElement>(
      "Nom de la bibliothèque",
    );
  }

  async function add(
    screen: Screen,
    reader: KnownReader,
    role: MembershipRole,
  ) {
    await fireEvent.click(
      await screen.findByRole("button", { name: "Ajouter un membre" }),
    );
    await fireEvent.click(
      within(
        screen.getByRole("list", { name: "Choisir un lecteur" }),
      ).getByRole("button", { name: reader.displayName }),
    );
    if (role === "OWNER")
      await fireEvent.click(
        screen.getByRole("button", { name: "Propriétaire" }),
      );
    await fireEvent.click(screen.getByRole("button", { name: "Ajouter" }));
  }

  async function ask(screen: Screen, isbn: string) {
    await fireEvent.input(
      await screen.findByRole("textbox", { name: "ISBN" }),
      { target: { value: isbn } },
    );
    await fireEvent.click(screen.getByRole("button", { name: "Chercher" }));
  }

  function row(screen: Screen, name: string) {
    return within(screen.getByRole("list", { name: "Mes bibliothèques" }))
      .getAllByRole("listitem")
      .find((item) => within(item).queryByText(name) !== null)!;
  }

  function rows(screen: Screen) {
    return listed(screen, "Mes bibliothèques");
  }

  function members(screen: Screen) {
    return listed(screen, "Membres");
  }

  function readersToChoose(screen: Screen) {
    return listed(screen, "Choisir un lecteur");
  }

  function listed(screen: Screen, list: string) {
    return within(screen.getByRole("list", { name: list }))
      .getAllByRole("listitem")
      .map(words);
  }

  function words(row: HTMLElement) {
    const texts = document.createTreeWalker(row, NodeFilter.SHOW_TEXT);
    const words: string[] = [];
    while (texts.nextNode()) {
      const text = texts.currentNode.textContent?.trim();
      if (text) words.push(text);
    }
    return words.join(" ");
  }
});
