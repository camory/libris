import type { Reader } from "../domain/Reader";

export const lea: Reader = {
  id: "0199c0de-1000-7000-8000-000000000002",
  username: "lea",
  displayName: "Léa",
  email: "lea@amory.fr",
  role: "READER",
  defaultBookshelf: {
    id: "0b1e2d3c-4f5a-4b6c-8d7e-9f0a1b2c3d4e",
    name: "Bibliothèque de Léa",
  },
};

export const chloe: Reader = {
  id: "0199c0de-1000-7000-8000-000000000001",
  username: "chloe",
  displayName: "Chloé",
  email: "chloe@amory.fr",
  role: "READER",
  defaultBookshelf: {
    id: "0b1e2d3c-4f5a-4b6c-8d7e-9f0a1b2c3d4f",
    name: "Bibliothèque de Chloé",
  },
};
