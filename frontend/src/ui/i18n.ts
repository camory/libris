import { createI18n } from "vue-i18n";

const fr = {
  home: {
    title: "La bibliothèque de la maison",
    greeting: "Bonjour {name}",
    add: "Ajouter un ouvrage",
  },
  isbn: {
    title: "Ajouter un ouvrage",
    hint: "Scannez le code-barres ou saisissez l'ISBN.",
    label: "ISBN",
    placeholder: "978-2-7234-8852-5",
    search: "Chercher",
    scan: "Scanner le code-barres",
    closeCamera: "Fermer la caméra",
    searching: "Recherche en cours…",
    invalid: "ISBN invalide",
    unknown: "ISBN inconnu",
    error: "Erreur lors de la recherche, veuillez réessayer plus tard.",
    card: {
      series: {
        BOOK: "{name} · tome {volume}",
        BD: "{name} · album {volume}",
        MANGA: "{name} · tome {volume}",
      },
      author: "{name} · {roles}",
      cover: "Couverture de {title}",
      copies: "Dans {bookshelf}",
      collection: "Collection",
      publisher: "Éditeur",
      year: "Année",
      language: "Langue",
      pages: "Pages",
      isbn: "ISBN",
    },
  },
  language: {
    fr: "français",
  },
  role: {
    BOOK: {
      WRITER: "texte",
      ARTIST: "illustration",
      COLOURIST: "couleurs",
      TRANSLATOR: "traduction",
    },
    BD: {
      WRITER: "scénario",
      ARTIST: "dessin",
      COLOURIST: "couleurs",
      TRANSLATOR: "traduction",
    },
    MANGA: {
      WRITER: "scénario",
      ARTIST: "dessin",
      COLOURIST: "couleurs",
      TRANSLATOR: "traduction",
    },
  },
  tabs: {
    home: "Accueil",
    add: "Ajouter",
  },
  update: {
    available: "Nouvelle version disponible",
    install: "Mettre à jour",
    installing: "Mise à jour…",
  },
};

export function createLibrisI18n() {
  return createI18n({
    legacy: false,
    locale: "fr",
    messages: { fr },
  });
}
