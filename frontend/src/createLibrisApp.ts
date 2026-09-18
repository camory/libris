import { createPinia } from "pinia";
import { createApp, type App } from "vue";
import type { RouteLocationNormalizedLoaded } from "vue-router";
import { appUpdateKey, type AppUpdate } from "./application/AppUpdate";
import {
  barcodeScannerKey,
  type BarcodeScanner,
} from "./application/BarcodeScanner";
import { isbnApiKey, type IsbnApi } from "./application/IsbnApi";
import { meApiKey, type MeApi } from "./application/MeApi";
import { revisionKey } from "./application/Revision";
import AppRoot from "./ui/App.vue";
import { createLibrisI18n } from "./ui/i18n";
import { createLibrisRouter } from "./ui/router";

export interface LibrisPorts {
  meApi: MeApi;
  isbnApi: IsbnApi;
  barcodeScanner: BarcodeScanner;
  appUpdate: AppUpdate;
}

export function createLibrisApp(ports: LibrisPorts, revision: string): App {
  const app = createApp(AppRoot);
  const router = createLibrisRouter();
  app.use(createPinia()).use(router).use(createLibrisI18n());
  router.currentRoute.value = router.resolve(
    router.options.history.location,
  ) as RouteLocationNormalizedLoaded;
  app.provide(meApiKey, ports.meApi);
  app.provide(isbnApiKey, ports.isbnApi);
  app.provide(barcodeScannerKey, ports.barcodeScanner);
  app.provide(appUpdateKey, ports.appUpdate);
  app.provide(revisionKey, revision);
  return app;
}
