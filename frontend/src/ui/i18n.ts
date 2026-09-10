import { createI18n } from "vue-i18n";

const fr = {
  home: {
    title: "La bibliothèque de la maison",
    greeting: "Bonjour {name}",
  },
};

export function createLibrisI18n() {
  return createI18n({
    legacy: false,
    locale: "fr",
    messages: { fr },
  });
}
