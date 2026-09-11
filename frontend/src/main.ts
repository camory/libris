import { bootstrap } from "./bootstrap";
import "./ui/style.css";

bootstrap(
  window.location.origin,
  import.meta.env.VITE_APP_VERSION ?? "dev",
).mount("#app");
