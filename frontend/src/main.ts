import { createLibrisApp } from "./createLibrisApp";
import { FetchMeApi } from "./infra/api/FetchMeApi";
import "./ui/style.css";

createLibrisApp(
  {
    meApi: new FetchMeApi(window.location.origin, () =>
      window.location.assign("/session"),
    ),
  },
  import.meta.env.VITE_APP_VERSION ?? "dev",
).mount("#app");
