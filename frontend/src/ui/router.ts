import { createRouter, createWebHistory } from "vue-router";
import CatalogueView from "./views/catalogue/CatalogueView.vue";
import HomeView from "./views/home/HomeView.vue";
import IsbnView from "./views/isbn/IsbnView.vue";

export function createLibrisRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      { path: "/", component: HomeView },
      { path: "/isbn", component: IsbnView },
      { path: "/catalogue", component: CatalogueView },
    ],
  });
}
