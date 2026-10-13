import type { App } from "vue";
import { createLibrisApp } from "./createLibrisApp";
import { FetchBookshelfApi } from "./infra/api/FetchBookshelfApi";
import { FetchBookshelvesApi } from "./infra/api/FetchBookshelvesApi";
import { FetchCatalogueApi } from "./infra/api/FetchCatalogueApi";
import { FetchIsbnApi } from "./infra/api/FetchIsbnApi";
import { FetchMeApi } from "./infra/api/FetchMeApi";
import { CameraBarcodeScanner } from "./infra/camera/CameraBarcodeScanner";
import { ServiceWorkerAppUpdate } from "./infra/pwa/ServiceWorkerAppUpdate";

export function bootstrap(origin: string, revision: string): App {
  return createLibrisApp(
    {
      meApi: new FetchMeApi(origin, () => window.location.assign("/session")),
      isbnApi: new FetchIsbnApi(origin),
      bookshelfApi: new FetchBookshelfApi(origin),
      barcodeScanner: new CameraBarcodeScanner(),
      appUpdate: new ServiceWorkerAppUpdate(() => window.location.reload()),
      catalogueApi: new FetchCatalogueApi(origin),
      bookshelvesApi: new FetchBookshelvesApi(origin),
    },
    revision,
  );
}
