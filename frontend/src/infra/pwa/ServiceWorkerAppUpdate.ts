import type { AppUpdate } from "../../application/AppUpdate";

export class ServiceWorkerAppUpdate implements AppUpdate {
  private announce: (() => void) | null = null;
  private waiting: ServiceWorker | null = null;

  constructor(private readonly reload: () => void) {
    navigator.serviceWorker.register("/sw.js").then((registration) => {
      this.found(registration.waiting);
      registration.addEventListener("updatefound", () => {
        const installing = registration.installing;
        installing?.addEventListener("statechange", () => {
          if (installing.state === "installed") {
            this.found(installing);
          }
        });
      });
    });
  }

  onNewVersion(announce: () => void): void {
    this.announce = announce;
    if (this.waiting !== null) {
      announce();
    }
  }

  install(): void {}

  private found(worker: ServiceWorker | null): void {
    if (worker === null || navigator.serviceWorker.controller === null) {
      return;
    }
    this.waiting = worker;
    this.announce?.();
  }
}
