import { createI18n } from "vue-i18n";

const fr = {
  home: {
    title: "La bibliothèque de la maison",
    greeting: "Bonjour {name}",
  },
  isbn: {
    title: "Ajouter un ouvrage",
    hint: "Scannez le code-barres ou saisissez l'ISBN.",
    label: "ISBN",
    placeholder: "978-2-7234-8852-5",
    search: "Chercher",
    invalid: "ISBN invalide",
    unknown: "ISBN inconnu",
    error: "Erreur lors de la recherche, veuillez réessayer plus tard.",
  },
};

export function createLibrisI18n() {
  return createI18n({
    legacy: false,
    locale: "fr",
    messages: { fr },
  });
}
