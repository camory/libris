import { createPinia } from "pinia";
import { createApp } from "vue";
import App from "./ui/App.vue";
import { i18n } from "./ui/i18n";
import { router } from "./ui/router";
import "./ui/style.css";

createApp(App).use(createPinia()).use(router).use(i18n).mount("#app");
