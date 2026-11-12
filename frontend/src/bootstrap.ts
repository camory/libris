import type { App } from "vue";
import { createLibrisApp } from "./createLibrisApp";
import { FetchAuthorsApi } from "./infra/api/FetchAuthorsApi";
import { FetchBookshelfApi } from "./infra/api/FetchBookshelfApi";
import { FetchBookshelvesApi } from "./infra/api/FetchBookshelvesApi";
import { FetchCatalogueApi } from "./infra/api/FetchCatalogueApi";
import { FetchCopiesApi } from "./infra/api/FetchCopiesApi";
import { FetchEditionsApi } from "./infra/api/FetchEditionsApi";
import { FetchIsbnApi } from "./infra/api/FetchIsbnApi";
import { FetchMeApi } from "./infra/api/FetchMeApi";
import { FetchReadersApi } from "./infra/api/FetchReadersApi";
import { FetchSeriesApi } from "./infra/api/FetchSeriesApi";
import { WebAudioBeeper } from "./infra/audio/WebAudioBeeper";
import { CameraBarcodeScanner } from "./infra/camera/CameraBarcodeScanner";
import { ServiceWorkerAppUpdate } from "./infra/pwa/ServiceWorkerAppUpdate";
import { WebStorageBeepSetting } from "./infra/storage/WebStorageBeepSetting";

export function bootstrap(origin: string, revision: string): App {
  const catalogueApi = new FetchCatalogueApi(origin);
  return createLibrisApp(
    {
      meApi: new FetchMeApi(origin, () => window.location.assign("/session")),
      isbnApi: new FetchIsbnApi(origin),
      bookshelfApi: new FetchBookshelfApi(origin),
      barcodeScanner: new CameraBarcodeScanner(),
      appUpdate: new ServiceWorkerAppUpdate(() => window.location.reload()),
      catalogueApi,
      bookshelvesApi: new FetchBookshelvesApi(origin),
      readersApi: new FetchReadersApi(origin),
      seriesApi: new FetchSeriesApi(origin),
      copiesApi: new FetchCopiesApi(origin),
      beeper: new WebAudioBeeper(() => new AudioContext()),
      beepSetting: new WebStorageBeepSetting(localStorage),
      authorsApi: new FetchAuthorsApi(origin),
      editionsApi: new FetchEditionsApi(origin),
      searchApi: catalogueApi,
    },
    revision,
  );
}
