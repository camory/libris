import { createI18n } from "vue-i18n";

const fr = {
  home: {
    title: "Libris",
    greeting: "Bonjour {name}",
    bookshelves: {
      label: "Mes bibliothèques",
      role: {
        OWNER: "propriétaire",
        VIEWER: "invité",
      },
      default: "{role} · par défaut",
      chooseDefault: "Choisir comme bibliothèque par défaut",
      choosingDefault: "Changement en cours…",
      defaultError: "Erreur lors du changement, veuillez réessayer plus tard.",
      error: "Erreur lors du chargement, veuillez réessayer plus tard.",
      new: "Nouvelle bibliothèque",
      name: "Nom de la bibliothèque",
      create: "Créer",
      creating: "Création en cours…",
      failure: {
        taken: "Vous avez déjà une bibliothèque de ce nom.",
        unavailable:
          "Erreur lors de la création, veuillez réessayer plus tard.",
      },
    },
  },
  bookshelf: {
    notFound: "Bibliothèque introuvable.",
    error: "Erreur lors du chargement, veuillez réessayer plus tard.",
    members: {
      label: "Membres",
      member: "{name} · {role}",
      you: "Vous",
      new: "Ajouter un membre",
      choose: "Choisir un lecteur",
      close: "Fermer",
      owner: "Propriétaire",
      add: "Ajouter",
      adding: "Ajout en cours…",
      unavailable: "Erreur lors de l'ajout, veuillez réessayer plus tard.",
      everyone: "Tous les lecteurs sont déjà membres.",
      role: {
        OWNER: "propriétaire",
        VIEWER: "invité",
      },
    },
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
    add: "Ajouter à ma bibliothèque",
    adding: "Ajout en cours…",
    invalid: "ISBN invalide",
    unknown: "ISBN inconnu",
    error: "Erreur lors de la recherche, veuillez réessayer plus tard.",
    addError: "Erreur lors de l'ajout, veuillez réessayer plus tard.",
    card: {
      series: {
        BOOK: "{name} · tome {volume}",
        BD: "{name} · album {volume}",
        MANGA: "{name} · tome {volume}",
      },
      author: "{name} · {roles}",
      cover: "Couverture de {title}",
      coverSource: "Couverture {source}",
      copies: "Dans {bookshelf} | Dans {bookshelf} · {count} exemplaires",
      noCopy: "Dans aucune de vos bibliothèques",
      collection: "Collection",
      publisher: "Éditeur",
      year: "Année",
      language: "Langue",
      pages: "Pages",
      isbn: "ISBN",
    },
  },
  catalogue: {
    title: "Parcourir le catalogue",
    hint: "Les ouvrages de toutes vos bibliothèques.",
    empty: "Les ouvrages de vos bibliothèques apparaîtront ici.",
    error: "Erreur lors du chargement, veuillez réessayer plus tard.",
    switch: {
      name: "Vue du catalogue",
      books: "Ouvrages",
      series: "Séries",
    },
    series: {
      BOOK: "{name} · tome {volume}",
      BD: "{name} · album {volume}",
      MANGA: "{name} · tome {volume}",
    },
    volume: {
      BOOK: "Tome {volume}",
      BD: "Album {volume}",
      MANGA: "Tome {volume}",
    },
    copies: "{bookshelf} | {bookshelf} · {count} exemplaires",
    cover: "Couverture de {title}",
  },
  series: {
    empty: "Les séries de vos bibliothèques apparaîtront ici.",
    error: "Erreur lors du chargement, veuillez réessayer plus tard.",
    cover: "Couverture de {name}",
    count: {
      BOOK: "{count} tome | {count} tomes",
      BD: "{count} album | {count} albums",
      MANGA: "{count} tome | {count} tomes",
      null: "{count} tome | {count} tomes",
    },
  },
  seriesPage: {
    hint: "{count} dans vos bibliothèques.",
    notFound: "Série introuvable.",
    owned: "Tomes possédés",
    missing: "Tomes manquants",
    run: "{first}",
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
    catalogue: "Catalogue",
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
