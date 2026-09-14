import { createI18n } from "vue-i18n";

const fr = {
  home: {
    title: "La bibliothèque de la maison",
    greeting: "Bonjour {name}",
  },
  isbn: {
    title: "Ajouter un ouvrage",
    hint: "Scannez le code-barres ou saisissez l'ISBN.",
  },
};

export function createLibrisI18n() {
  return createI18n({
    legacy: false,
    locale: "fr",
    messages: { fr },
  });
}
