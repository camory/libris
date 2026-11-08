import {
  fireEvent,
  queries,
  within,
  type BoundFunctions,
} from "@testing-library/dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { App } from "vue";
import type { BarcodeScanner } from "../application/BarcodeScanner";
import type { IsbnAnswer, IsbnApi } from "../application/IsbnApi";
import { createLibrisApp } from "../createLibrisApp";
import { FakeAppUpdate } from "../fixture/FakeAppUpdate";
import { FakeBarcodeScanner } from "../fixture/FakeBarcodeScanner";
import { FakeBeeper } from "../fixture/FakeBeeper";
import { FakeBookshelfApi } from "../fixture/FakeBookshelfApi";
import { FakeBookshelvesApi } from "../fixture/FakeBookshelvesApi";
import { FakeCatalogueApi } from "../fixture/FakeCatalogueApi";
import { FakeCopiesApi } from "../fixture/FakeCopiesApi";
import { FakeIsbnApi } from "../fixture/FakeIsbnApi";
import { FakeMeApi } from "../fixture/FakeMeApi";
import { FakeReadersApi } from "../fixture/FakeReadersApi";
import { FakeSeriesApi } from "../fixture/FakeSeriesApi";
import { lea } from "../fixture/Readers";
import { onePiece1 } from "../fixture/SourceEditions";

type Screen = BoundFunctions<typeof queries>;

describe("Scan beep", () => {
  const host = document.createElement("div");
  let app: App;

  afterEach(() => {
    vi.useRealTimers();
    app.unmount();
    window.history.replaceState(null, "", "/");
    localStorage.clear();
  });

  it("S1 The trick turns the beep on", async () => {
    // Given
    const beeper = new FakeBeeper();
    const camera = theCameraOpen();
    const screen = await open({ beeper, camera });

    // When
    await tapTheTitleThreeTimesInARow(screen);

    // Then
    expect(beeper.beeps).toBe(1);

    // When
    camera.reads(onePiece1.isbn13);

    // Then
    await screen.findByText("Romance dawn");
    expect(beeper.beeps).toBe(2);
  });

  it.skip("S2 Three slow taps change nothing", async () => {
    // Given
    const beeper = new FakeBeeper();
    const camera = theCameraOpen();
    const screen = await open({ beeper, camera });

    // When
    vi.useFakeTimers({
      toFake: ["Date", "performance", "setTimeout", "clearTimeout"],
    });
    await fireEvent.click(title(screen));
    vi.advanceTimersByTime(600);
    await fireEvent.click(title(screen));
    vi.advanceTimersByTime(600);
    await fireEvent.click(title(screen));
    vi.useRealTimers();

    // Then
    expect(beeper.beeps).toBe(0);

    // When
    camera.reads(onePiece1.isbn13);

    // Then
    await screen.findByText("Romance dawn");
    expect(beeper.beeps).toBe(0);
  });

  it("S3 The beep off, the camera stays silent", async () => {
    // Given
    const beeper = new FakeBeeper();
    const camera = theCameraOpen();
    const isbn = theHouseHolds();
    const screen = await open({ beeper, camera, isbn });

    // When
    camera.reads(onePiece1.isbn13);

    // Then
    await screen.findByText("Romance dawn");
    expect(beeper.beeps).toBe(0);
    expect(isbn.asked).toEqual([onePiece1.isbn13]);
  });

  it("S4 The beep on, the camera beeps", async () => {
    // Given
    const beeper = new FakeBeeper();
    const camera = theCameraOpen();
    const libris = librisAnswersWhenTold();
    const screen = await open({ beeper, camera, isbn: libris.isbn });
    await tapTheTitleThreeTimesInARow(screen);
    expect(beeper.beeps).toBe(1);

    // When
    camera.reads(onePiece1.isbn13);

    // Then
    await vi.waitFor(() => expect(beeper.beeps).toBe(2));
    expect(screen.queryByText("Romance dawn")).toBeNull();

    // When
    libris.answers();

    // Then
    await screen.findByText("Romance dawn");
    expect(libris.isbn.asked).toEqual([onePiece1.isbn13]);
    expect(beeper.beeps).toBe(2);
  });

  it.skip("S5 The trick again turns the beep off", async () => {
    // Given
    const beeper = new FakeBeeper();
    const camera = theCameraOpen();
    const screen = await open({ beeper, camera });
    await tapTheTitleThreeTimesInARow(screen);
    expect(beeper.beeps).toBe(1);

    // When
    await tapTheTitleThreeTimesInARow(screen);

    // Then
    expect(beeper.beeps).toBe(1);

    // When
    camera.reads(onePiece1.isbn13);

    // Then
    await screen.findByText("Romance dawn");
    expect(beeper.beeps).toBe(1);
  });

  it.skip("S6 The beep stays on when Libris is opened again", async () => {
    // Given
    const firstBeeper = new FakeBeeper();
    const first = await open({ beeper: firstBeeper, camera: theCameraOpen() });
    await tapTheTitleThreeTimesInARow(first);
    expect(firstBeeper.beeps).toBe(1);
    app.unmount();

    // When
    const beeper = new FakeBeeper();
    const camera = theCameraOpen();
    const screen = await open({ beeper, camera });
    camera.reads(onePiece1.isbn13);

    // Then
    await screen.findByText("Romance dawn");
    expect(beeper.beeps).toBe(1);
  });

  it.skip("S7 Only the camera's read beeps, an ISBN typed", async () => {
    // Given
    const beeper = new FakeBeeper();
    const screen = await open({ beeper, camera: theCameraOpen() });
    await tapTheTitleThreeTimesInARow(screen);
    expect(beeper.beeps).toBe(1);

    // When
    await fireEvent.input(screen.getByRole("textbox", { name: "ISBN" }), {
      target: { value: onePiece1.isbn13 },
    });
    await fireEvent.click(screen.getByRole("button", { name: "Chercher" }));

    // Then
    await screen.findByText("Romance dawn");
    expect(beeper.beeps).toBe(1);
  });

  it.skip("S7 Only the camera's read beeps, the camera closed", async () => {
    // Given
    const beeper = new FakeBeeper();
    const screen = await open({ beeper, camera: theCameraOpen() });
    await tapTheTitleThreeTimesInARow(screen);
    expect(beeper.beeps).toBe(1);

    // When
    await fireEvent.click(
      screen.getByRole("button", { name: "Fermer la caméra" }),
    );

    // Then
    await screen.findByRole("button", { name: "Scanner le code-barres" });
    expect(beeper.beeps).toBe(1);
  });

  function theCameraOpen() {
    let resolve!: (isbn13: string) => void;
    const read = new Promise<string>((done) => {
      resolve = done;
    });
    return {
      scanner: new FakeBarcodeScanner(true, read),
      reads: (isbn13: string) => resolve(isbn13),
    };
  }

  function found(): IsbnAnswer {
    return {
      outcome: "found",
      id: null,
      edition: onePiece1,
      copies: [],
      covers: [],
    };
  }

  function theHouseHolds(): FakeIsbnApi {
    return new FakeIsbnApi(found());
  }

  function librisAnswersWhenTold() {
    let resolve!: (answer: IsbnAnswer) => void;
    const answer = new Promise<IsbnAnswer>((done) => {
      resolve = done;
    });
    return {
      isbn: new FakeIsbnApi(answer),
      answers: () => resolve(found()),
    };
  }

  async function open(world: {
    beeper: FakeBeeper;
    camera: { scanner: BarcodeScanner };
    isbn?: IsbnApi;
  }) {
    window.history.replaceState(null, "", "/isbn");
    app = createLibrisApp(
      {
        meApi: new FakeMeApi(lea),
        isbnApi: world.isbn ?? theHouseHolds(),
        bookshelfApi: new FakeBookshelfApi(
          new Error("no add in this scenario"),
        ),
        barcodeScanner: world.camera.scanner,
        appUpdate: new FakeAppUpdate(),
        catalogueApi: new FakeCatalogueApi([]),
        bookshelvesApi: new FakeBookshelvesApi({ memberships: [] }),
        readersApi: new FakeReadersApi([]),
        seriesApi: new FakeSeriesApi([]),
        copiesApi: new FakeCopiesApi(new Error("no removal in this scenario")),
        beeper: world.beeper,
      },
      "sha-abc1234",
    );
    app.mount(host);
    const screen = within(host);
    await screen.findByRole("button", { name: "Fermer la caméra" });
    return screen;
  }

  function title(screen: Screen) {
    return screen.getByRole("heading", {
      level: 1,
      name: "Ajouter un ouvrage",
    });
  }

  async function tapTheTitleThreeTimesInARow(screen: Screen) {
    await fireEvent.click(title(screen));
    await fireEvent.click(title(screen));
    await fireEvent.click(title(screen));
  }
});
