import { createI18n } from "vue-i18n";

const fr = {
  home: {
    title: "La bibliothèque de la maison",
    greeting: "Bonjour {name}",
  },
};

export const i18n = createI18n({
  legacy: false,
  locale: "fr",
  messages: { fr },
});
