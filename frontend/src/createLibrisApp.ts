import { LUCIDE_CONTEXT } from "@lucide/vue";
import { createPinia } from "pinia";
import { createApp, type App } from "vue";
import type { RouteLocationNormalizedLoaded } from "vue-router";
import { appUpdateKey, type AppUpdate } from "./application/AppUpdate";
import { authorsApiKey, type AuthorsApi } from "./application/AuthorsApi";
import { bookshelfApiKey, type BookshelfApi } from "./application/BookshelfApi";
import {
  barcodeScannerKey,
  type BarcodeScanner,
} from "./application/BarcodeScanner";
import { beeperKey, type Beeper } from "./application/Beeper";
import { beepSettingKey, type BeepSetting } from "./application/BeepSetting";
import {
  bookshelvesApiKey,
  type BookshelvesApi,
} from "./application/BookshelvesApi";
import { catalogueApiKey, type CatalogueApi } from "./application/CatalogueApi";
import { copiesApiKey, type CopiesApi } from "./application/CopiesApi";
import { editionsApiKey, type EditionsApi } from "./application/EditionsApi";
import { isbnApiKey, type IsbnApi } from "./application/IsbnApi";
import { meApiKey, type MeApi } from "./application/MeApi";
import { readersApiKey, type ReadersApi } from "./application/ReadersApi";
import { revisionKey } from "./application/Revision";
import { seriesApiKey, type SeriesApi } from "./application/SeriesApi";
import AppRoot from "./ui/App.vue";
import { createLibrisI18n } from "./ui/i18n";
import { createLibrisRouter } from "./ui/router";

export interface LibrisPorts {
  meApi: MeApi;
  isbnApi: IsbnApi;
  bookshelfApi: BookshelfApi;
  barcodeScanner: BarcodeScanner;
  appUpdate: AppUpdate;
  catalogueApi: CatalogueApi;
  bookshelvesApi: BookshelvesApi;
  readersApi: ReadersApi;
  seriesApi: SeriesApi;
  copiesApi: CopiesApi;
  beeper: Beeper;
  beepSetting: BeepSetting;
  authorsApi: AuthorsApi;
  editionsApi: EditionsApi;
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
  app.provide(bookshelfApiKey, ports.bookshelfApi);
  app.provide(barcodeScannerKey, ports.barcodeScanner);
  app.provide(appUpdateKey, ports.appUpdate);
  app.provide(catalogueApiKey, ports.catalogueApi);
  app.provide(bookshelvesApiKey, ports.bookshelvesApi);
  app.provide(readersApiKey, ports.readersApi);
  app.provide(seriesApiKey, ports.seriesApi);
  app.provide(copiesApiKey, ports.copiesApi);
  app.provide(beeperKey, ports.beeper);
  app.provide(beepSettingKey, ports.beepSetting);
  app.provide(authorsApiKey, ports.authorsApi);
  app.provide(editionsApiKey, ports.editionsApi);
  app.provide(revisionKey, revision);
  app.provide(LUCIDE_CONTEXT, { strokeWidth: 1.8 });
  return app;
}
