import type { AppUpdate } from "../../application/AppUpdate";

export class ServiceWorkerAppUpdate implements AppUpdate {
  private announce: (() => void) | null = null;

  constructor(private readonly reload: () => void) {
    navigator.serviceWorker.register("/sw.js").then((registration) => {
      registration.addEventListener("updatefound", () => {
        const installing = registration.installing;
        installing?.addEventListener("statechange", () => {
          if (installing.state === "installed") {
            this.announce?.();
          }
        });
      });
    });
  }

  onNewVersion(announce: () => void): void {
    this.announce = announce;
  }

  install(): void {}
}
