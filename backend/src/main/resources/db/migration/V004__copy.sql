CREATE TABLE copy (
    id           uuid PRIMARY KEY,
    edition_id   uuid NOT NULL REFERENCES edition (id) ON DELETE CASCADE,
    bookshelf_id uuid NOT NULL REFERENCES bookshelf (id) ON DELETE CASCADE
);
