import type { App } from "vue";
import { createLibrisApp } from "./createLibrisApp";
import { FetchMeApi } from "./infra/api/FetchMeApi";

export function bootstrap(origin: string, revision: string): App {
  return createLibrisApp(
    {
      meApi: new FetchMeApi(origin, () => window.location.assign("/session")),
    },
    revision,
  );
}
