import type { AppUpdate } from "../../application/AppUpdate";

export class ServiceWorkerAppUpdate implements AppUpdate {
  private announce: (() => void) | null = null;
  private waiting: ServiceWorker | null = null;
  private reloaded = false;

  constructor(private readonly reload: () => void) {
    if (!("serviceWorker" in navigator)) {
      return;
    }
    navigator.serviceWorker.addEventListener("controllerchange", () => {
      this.takeOver();
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
    this.found(registration.waiting);
    registration.addEventListener("updatefound", () => {
      const installing = registration.installing;
      installing?.addEventListener("statechange", () => {
        if (installing.state === "installed") {
          this.found(installing);
        }
      });
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
