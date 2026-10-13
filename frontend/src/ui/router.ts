import { createRouter, createWebHistory } from "vue-router";
import BookshelfView from "./views/bookshelf/BookshelfView.vue";
import CatalogueView from "./views/catalogue/CatalogueView.vue";
import HomeView from "./views/home/HomeView.vue";
import IsbnView from "./views/isbn/IsbnView.vue";

export function createLibrisRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [
      {
        path: "/",
        children: [
          { path: "", component: HomeView },
          { path: "bibliotheques/:id", component: BookshelfView, props: true },
        ],
      },
      { path: "/isbn", component: IsbnView },
      { path: "/catalogue", component: CatalogueView },
    ],
  });
}
