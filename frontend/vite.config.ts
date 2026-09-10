import tailwindcss from "@tailwindcss/vite";
import vue from "@vitejs/plugin-vue";
import { defineConfig } from "vitest/config";
import { VitePWA } from "vite-plugin-pwa";

export default defineConfig(({ mode }) => ({
  plugins: [
    vue(),
    tailwindcss(),
    VitePWA({
      registerType: "autoUpdate",
      manifest: {
        name: "Libris",
        short_name: "Libris",
        lang: "fr",
        display: "standalone",
        start_url: "/",
        theme_color: "#1e293b",
        background_color: "#ffffff",
        icons: [
          { src: "pwa-192x192.png", sizes: "192x192", type: "image/png" },
          { src: "pwa-512x512.png", sizes: "512x512", type: "image/png" },
        ],
      },
    }),
  ],
  server: {
    proxy: {
      "/api": {
        target:
          mode === "mock" ? "http://localhost:9090" : "http://localhost:8080",
        headers: {
          "Remote-User": "dev",
          "Remote-Name": "Dev Admin",
          "Remote-Email": "dev@amory.fr",
          "Remote-Groups": "libris-admin",
        },
      },
    },
  },
  test: {
    environment: "jsdom",
    globalSetup: ["./vitest.global-setup.ts"],
    coverage: {
      provider: "v8",
      reporter: ["text", "lcov"],
    },
  },
}));
