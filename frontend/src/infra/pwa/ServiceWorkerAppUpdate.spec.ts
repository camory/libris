import { afterEach, describe, expect, it, vi } from "vitest";
import {
  betweenChecks,
  ServiceWorkerAppUpdate,
} from "./ServiceWorkerAppUpdate";

const aMinute = 60_000;

describe("ServiceWorkerAppUpdate", () => {
  afterEach(() => {
    vi.useRealTimers();
    Reflect.deleteProperty(navigator, "serviceWorker");
  });

  it("announces the version that becomes ready while the app runs", async () => {
    // Given
    const browser = browserRunningAWorker();
    const announced = vi.fn();
    new ServiceWorkerAppUpdate(vi.fn()).onNewVersion(announced);
    await settled();

    // When
    await browser.aNewerVersionIsFound();
    await settled();

    // Then
    expect(announced).toHaveBeenCalledTimes(1);
  });

  it("announces the version already waiting when the app starts", async () => {
    // Given
    const browser = browserRunningAWorker();
    browser.aVersionIsAlreadyWaiting();
    const update = new ServiceWorkerAppUpdate(vi.fn());
    await settled();

    // When
    const announced = vi.fn();
    update.onNewVersion(announced);

    // Then
    expect(announced).toHaveBeenCalledTimes(1);
  });

  it("announces the version found before the registration settles", async () => {
    // Given
    const browser = browserRunningAWorker();
    const announced = vi.fn();
    new ServiceWorkerAppUpdate(vi.fn()).onNewVersion(announced);

    // When
    await browser.aNewerVersionIsFound();
    await settled();

    // Then
    expect(announced).toHaveBeenCalledTimes(1);
  });

  it("announces nothing when the new version fails to install", async () => {
    // Given
    const browser = browserRunningAWorker();
    const announced = vi.fn();
    new ServiceWorkerAppUpdate(vi.fn()).onNewVersion(announced);
    await settled();

    // When
    await browser.aNewerVersionFailsToInstall();
    await settled();

    // Then
    expect(announced).not.toHaveBeenCalled();
  });

  it("announces nothing when the first worker installs", async () => {
    // Given
    const browser = browserInstallingItsFirstWorker();
    const announced = vi.fn();
    new ServiceWorkerAppUpdate(vi.fn()).onNewVersion(announced);
    await settled();

    // When
    await browser.aNewerVersionIsFound();
    await settled();

    // Then
    expect(announced).not.toHaveBeenCalled();
  });

  it("tells the waiting worker to take over on the order", async () => {
    // Given
    const browser = browserRunningAWorker();
    const reload = vi.fn();
    const update = new ServiceWorkerAppUpdate(reload);
    update.onNewVersion(vi.fn());
    await settled();
    const newWorker = await browser.aNewerVersionIsFound();
    await settled();

    // When
    update.install();

    // Then
    expect(newWorker.postMessage).toHaveBeenCalledTimes(1);
    expect(newWorker.postMessage).toHaveBeenCalledWith({
      type: "SKIP_WAITING",
    });
    expect(reload).not.toHaveBeenCalled();
  });

  it("reloads once when the new worker takes control", async () => {
    // Given
    const browser = browserRunningAWorker();
    const reload = vi.fn();
    new ServiceWorkerAppUpdate(reload);
    await settled();

    // When
    browser.theNewWorkerTakesControl();

    // Then
    expect(reload).toHaveBeenCalledTimes(1);
  });

  it("reloads no more than once", async () => {
    // Given
    const browser = browserRunningAWorker();
    const reload = vi.fn();
    new ServiceWorkerAppUpdate(reload);
    await settled();
    browser.theNewWorkerTakesControl();

    // When
    browser.theNewWorkerTakesControl();

    // Then
    expect(reload).toHaveBeenCalledTimes(1);
  });

  it("does nothing where the browser has no service worker", () => {
    // Given
    const announced = vi.fn();

    // When
    const update = new ServiceWorkerAppUpdate(vi.fn());
    update.onNewVersion(announced);
    update.install();

    // Then
    expect(announced).not.toHaveBeenCalled();
  });

  it("announces nothing when the registration fails", async () => {
    // Given
    browserRefusingToRegister();
    const announced = vi.fn();

    // When
    new ServiceWorkerAppUpdate(vi.fn()).onNewVersion(announced);
    await settled();

    // Then
    expect(announced).not.toHaveBeenCalled();
  });

  it("asks for a newer version an hour after it registered", async () => {
    // Given
    vi.useFakeTimers();
    const browser = browserRunningAWorker();
    new ServiceWorkerAppUpdate(vi.fn());
    await vi.advanceTimersByTimeAsync(0);

    // When
    await vi.advanceTimersByTimeAsync(betweenChecks);

    // Then
    expect(browser.asked).toHaveBeenCalledTimes(1);
  });

  function browserRefusingToRegister() {
    const container = new EventTarget() as FakeContainer;
    container.controller = aWorker("activated");
    container.register = () => Promise.reject(new Error("no worker"));
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: container,
    });
  }

  function browserRunningAWorker() {
    return aBrowser(aWorker("activated"));
  }

  function browserInstallingItsFirstWorker() {
    return aBrowser(null);
  }

  function aBrowser(controller: FakeWorker | null) {
    const container = new EventTarget() as FakeContainer;
    container.controller = controller;
    const registration = new EventTarget() as FakeRegistration;
    registration.installing = null;
    registration.waiting = null;
    registration.update = vi.fn().mockResolvedValue(undefined);
    container.register = () => Promise.resolve(registration);
    Object.defineProperty(navigator, "serviceWorker", {
      configurable: true,
      value: container,
    });

    return {
      asked: registration.update,

      aVersionIsAlreadyWaiting() {
        registration.waiting = aWorker("installed");
      },

      theNewWorkerTakesControl() {
        container.dispatchEvent(new Event("controllerchange"));
      },

      async aNewerVersionFailsToInstall() {
        const worker = aWorker("installing");
        registration.installing = worker;
        registration.dispatchEvent(new Event("updatefound"));
        await Promise.resolve();
        worker.state = "redundant";
        registration.installing = null;
        worker.dispatchEvent(new Event("statechange"));
      },

      async aNewerVersionIsFound() {
        const worker = aWorker("installing");
        registration.installing = worker;
        registration.dispatchEvent(new Event("updatefound"));
        await Promise.resolve();
        worker.state = "installed";
        registration.installing = null;
        registration.waiting = worker;
        worker.dispatchEvent(new Event("statechange"));
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
    update: ReturnType<typeof vi.fn>;
  };

  type FakeContainer = EventTarget & {
    controller: FakeWorker | null;
    register: () => Promise<FakeRegistration>;
  };

  function aWorker(state: string): FakeWorker {
    const worker = new EventTarget() as FakeWorker;
    worker.state = state;
    worker.postMessage = vi.fn();
    return worker;
  }

  function settled(): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, 0));
  }
});
