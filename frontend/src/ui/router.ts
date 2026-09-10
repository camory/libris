import { createRouter, createWebHistory } from "vue-router";
import HomeView from "./views/home/HomeView.vue";

export function createLibrisRouter() {
  return createRouter({
    history: createWebHistory(),
    routes: [{ path: "/", component: HomeView }],
  });
}
