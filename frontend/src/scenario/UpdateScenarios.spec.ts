import { fireEvent, within } from "@testing-library/dom";
import { afterEach, describe, expect, it, vi } from "vitest";
import type { App } from "vue";
import { createLibrisApp } from "../createLibrisApp";
import { FakeBarcodeScanner } from "../fixture/FakeBarcodeScanner";
import { FakeBookshelfApi } from "../fixture/FakeBookshelfApi";
import { FakeIsbnApi } from "../fixture/FakeIsbnApi";
import { FakeMeApi } from "../fixture/FakeMeApi";
import { lea } from "../fixture/Readers";
import { ServiceWorkerAppUpdate } from "../infra/pwa/ServiceWorkerAppUpdate";

describe("Update", () => {
  const host = document.createElement("div");
  let app: App;

  afterEach(() => {
    app.unmount();
    window.history.replaceState(null, "", "/");
    vi.restoreAllMocks();
    Reflect.deleteProperty(navigator, "serviceWorker");
  });

  it("S1 A new version is ready", async () => {
    // Given
    const browser = browserRunsTheApp();
    const screen = open("/");
    await screen.findByText("Bonjour Léa");

    // When
    await browser.aNewerVersionIsFound();

    // Then
    await screen.findByText("Nouvelle version disponible");
    expect(screen.getByRole("button", { name: "Mettre à jour" })).toBeDefined();
    expect(screen.getByText("Bonjour Léa")).toBeDefined();
  });

  it("S2 The reader updates", async () => {
    // Given
    const browser = browserRunsTheApp();
    const screen = open("/");
    const newWorker = await browser.aNewerVersionIsFound();
    await screen.findByText("Nouvelle version disponible");

    // When
    await fireEvent.click(
      screen.getByRole("button", { name: "Mettre à jour" }),
    );

    // Then
    await screen.findByText("Mise à jour…");
    expect(newWorker.postMessage).toHaveBeenCalledTimes(1);
    expect(newWorker.postMessage).toHaveBeenCalledWith({
      type: "SKIP_WAITING",
    });
    expect(screen.queryByRole("button", { name: "Mettre à jour" })).toBeNull();
  });

  it("S4 Nothing new", async () => {
    // Given
    browserRunsTheApp();

    // When
    const screen = open("/");
    await screen.findByText("Bonjour Léa");

    // Then
    expect(screen.queryByText("Nouvelle version disponible")).toBeNull();
    expect(screen.queryByText("Mise à jour…")).toBeNull();
  });

  function browserRunsTheApp() {
    const container = new EventTarget() as EventTarget & {
      controller: FakeWorker | null;
      register: () => Promise<FakeRegistration>;
    };
    container.controller = aWorker("activated");
    const registration = new EventTarget() as FakeRegistration;
    registration.installing = null;
    registration.waiting = null;
    container.register = () => Promise.resolve(registration);
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: container,
    });

    return {
      async aNewerVersionIsFound() {
        const worker = aWorker("installing");
        worker.postMessage.mockImplementation((message: { type: string }) => {
          if (message.type === "SKIP_WAITING") {
            worker.state = "activated";
            registration.waiting = null;
            container.controller = worker;
            container.dispatchEvent(new Event("controllerchange"));
          }
        });
        registration.installing = worker;
        registration.dispatchEvent(new Event("updatefound"));
        await Promise.resolve();
        worker.state = "installed";
        registration.installing = null;
        registration.waiting = worker;
        worker.dispatchEvent(new Event("statechange"));
        await Promise.resolve();
        return worker;
      },
    };
  }

  type FakeWorker = EventTarget & {
    state: string;
    postMessage: ReturnType<typeof vi.fn>;
  };

  type FakeRegistration = EventTarget & {
    installing: FakeWorker | null;
    waiting: FakeWorker | null;
  };

  function aWorker(state: string): FakeWorker {
    const worker = new EventTarget() as FakeWorker;
    worker.state = state;
    worker.postMessage = vi.fn();
    return worker;
  }

  function open(path: string) {
    window.history.replaceState(null, "", path);
    app = createLibrisApp(
      {
        meApi: new FakeMeApi(lea),
        isbnApi: new FakeIsbnApi({
          outcome: "problem",
          type: "/problems/not-found",
        }),
        bookshelfApi: new FakeBookshelfApi(
          new Error("no add in this scenario"),
        ),
        barcodeScanner: new FakeBarcodeScanner(false),
        appUpdate: new ServiceWorkerAppUpdate(() => {}),
      },
      "sha-abc1234",
    );
    app.mount(host);
    return within(host);
  }
});
