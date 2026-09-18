import type { AppUpdate } from "../../application/AppUpdate";

export const betweenChecks = 3_600_000;

export class ServiceWorkerAppUpdate implements AppUpdate {
  private announce: (() => void) | null = null;
  private waiting: ServiceWorker | null = null;
  private reloaded = false;
  private registration: ServiceWorkerRegistration | null = null;
  private pendingCheck: ReturnType<typeof setTimeout> | null = null;

  constructor(private readonly reload: () => void) {
    if (!("serviceWorker" in navigator)) {
      return;
    }
    navigator.serviceWorker.addEventListener("controllerchange", () => {
      this.takeOver();
    });
    document.addEventListener("visibilitychange", () => {
      if (document.visibilityState === "visible") {
        this.check();
      } else {
        this.cancelCheck();
      }
    });
    navigator.serviceWorker
      .register("/sw.js")
      .then((registration) => {
        this.watch(registration);
      })
      .catch(() => {});
  }

  onNewVersion(announce: () => void): void {
    this.announce = announce;
    if (this.waiting !== null) {
      announce();
    }
  }

  install(): void {
    this.waiting?.postMessage({ type: "SKIP_WAITING" });
  }

  private watch(registration: ServiceWorkerRegistration): void {
    this.registration = registration;
    this.found(registration.waiting);
    this.whenInstalled(registration.installing);
    registration.addEventListener("updatefound", () => {
      this.whenInstalled(registration.installing);
    });
    this.scheduleCheck();
  }

  private cancelCheck(): void {
    if (this.pendingCheck !== null) {
      clearTimeout(this.pendingCheck);
      this.pendingCheck = null;
    }
  }

  private scheduleCheck(): void {
    this.cancelCheck();
    this.pendingCheck = setTimeout(() => {
      this.check();
    }, betweenChecks);
  }

  private check(): void {
    this.registration?.update().catch(() => {});
    this.scheduleCheck();
  }

  private whenInstalled(worker: ServiceWorker | null): void {
    worker?.addEventListener("statechange", () => {
      if (worker.state === "installed") {
        this.found(worker);
      }
    });
  }

  private takeOver(): void {
    if (this.reloaded) {
      return;
    }
    this.reloaded = true;
    this.reload();
  }

  private found(worker: ServiceWorker | null): void {
    if (worker === null || navigator.serviceWorker.controller === null) {
      return;
    }
    this.waiting = worker;
    this.announce?.();
  }
}
