import { createPinia } from "pinia";
import { createApp, type App } from "vue";
import type { RouteLocationNormalizedLoaded } from "vue-router";
import {
  barcodeScannerKey,
  type BarcodeScanner,
} from "./application/BarcodeScanner";
import { isbnApiKey, type IsbnApi } from "./application/IsbnApi";
import { meApiKey, type MeApi } from "./application/MeApi";
import AppRoot from "./ui/App.vue";
import { createLibrisI18n } from "./ui/i18n";
import { revisionKey } from "./ui/revision";
import { createLibrisRouter } from "./ui/router";

export interface LibrisPorts {
  meApi: MeApi;
  isbnApi: IsbnApi;
  barcodeScanner: BarcodeScanner;
}

export function createLibrisApp(ports: LibrisPorts, revision: string): App {
  const app = createApp(AppRoot, { revision });
  const router = createLibrisRouter();
  app.use(createPinia()).use(router).use(createLibrisI18n());
  router.currentRoute.value = router.resolve(
    router.options.history.location,
  ) as RouteLocationNormalizedLoaded;
  app.provide(meApiKey, ports.meApi);
  app.provide(isbnApiKey, ports.isbnApi);
  app.provide(barcodeScannerKey, ports.barcodeScanner);
  app.provide(revisionKey, revision);
  return app;
}
