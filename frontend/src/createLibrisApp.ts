import { createPinia } from "pinia";
import { createApp, type App } from "vue";
import { meApiKey, type MeApi } from "./application/MeApi";
import AppRoot from "./ui/App.vue";
import { createLibrisI18n } from "./ui/i18n";
import { createLibrisRouter } from "./ui/router";

export interface LibrisPorts {
  meApi: MeApi;
}

export function createLibrisApp(ports: LibrisPorts, revision: string): App {
  const app = createApp(AppRoot, { revision });
  app.use(createPinia()).use(createLibrisRouter()).use(createLibrisI18n());
  app.provide(meApiKey, ports.meApi);
  return app;
}
