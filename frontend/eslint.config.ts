import boundaries from "eslint-plugin-boundaries";
import pluginVue from "eslint-plugin-vue";
import tseslint from "typescript-eslint";

export default tseslint.config(
  { ignores: ["dist", "dev-dist", "coverage"] },
  tseslint.configs.recommended,
  pluginVue.configs["flat/essential"],
  {
    files: ["**/*.vue"],
    languageOptions: { parserOptions: { parser: tseslint.parser } },
  },
  {
    files: ["src/**"],
    plugins: { boundaries },
    settings: {
      "import/resolver": { node: { extensions: [".ts", ".vue"] } },
      "boundaries/elements": [
        { type: "domain", pattern: "src/domain" },
        { type: "application", pattern: "src/application" },
        { type: "infra", pattern: "src/infra" },
        { type: "ui-components", pattern: "src/ui/components" },
        { type: "ui-views", pattern: "src/ui/views/*", capture: ["view"] },
        { type: "ui-shell", pattern: "src/ui" },
        { type: "fixture", pattern: "src/fixture" },
        { type: "main", pattern: "src" },
      ],
      "boundaries/files": [
        { category: "router", pattern: "src/ui/router.ts" },
        { category: "test", pattern: "**/*.spec.ts" },
      ],
    },
    rules: {
      "boundaries/dependencies": [
        "error",
        {
          default: "disallow",
          checkAllOrigins: true,
          policies: [
            {
              from: { element: { type: "!domain" } },
              allow: { to: { module: { origin: "external" } } },
            },
            {
              from: { element: { type: "application" } },
              allow: { to: { element: { type: "domain" } } },
            },
            {
              from: { element: { type: "infra" } },
              allow: { to: { element: { types: ["domain", "application"] } } },
            },
            {
              from: { element: { type: "fixture" } },
              allow: { to: { element: { types: ["domain", "application"] } } },
            },
            {
              from: { element: { type: "ui-components" } },
              allow: { to: { element: { type: "domain" } } },
            },
            {
              from: { element: { type: "ui-views" } },
              allow: {
                to: {
                  element: {
                    types: ["domain", "application", "ui-components"],
                  },
                },
              },
            },
            {
              from: { element: { type: "ui-shell" } },
              allow: { to: { element: { type: "ui-components" } } },
            },
            {
              from: { file: { categories: "router" } },
              allow: { to: { element: { type: "ui-views" } } },
            },
            {
              from: { element: { type: "main" } },
              allow: { to: { element: { type: "*" } } },
            },
            {
              from: { file: { categories: "test" } },
              allow: [
                { to: { element: { type: "*" } } },
                { to: { module: { origin: "external" } } },
              ],
            },
          ],
        },
      ],
    },
  },
);
