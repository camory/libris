CREATE TABLE series (
    id   uuid PRIMARY KEY,
    name text NOT NULL
);

CREATE UNIQUE INDEX series_name_unique ON series (LOWER(name));

CREATE TABLE author (
    id   uuid PRIMARY KEY,
    name text NOT NULL
);

CREATE UNIQUE INDEX author_name_unique ON author (LOWER(name));

CREATE TABLE edition (
    id               uuid PRIMARY KEY,
    isbn13           text UNIQUE,
    kind             text NOT NULL,
    title            text NOT NULL,
    subtitle         text,
    series_id        uuid REFERENCES series (id),
    volume_number    int,
    collection       text,
    publisher        text,
    publication_year int,
    language         text,
    page_count       int,
    summary          text,
    cover_url        text
);

CREATE TABLE contribution (
    edition_id uuid NOT NULL REFERENCES edition (id) ON DELETE CASCADE,
    author_id  uuid NOT NULL REFERENCES author (id),
    role       text NOT NULL,
    PRIMARY KEY (edition_id, author_id, role)
);
