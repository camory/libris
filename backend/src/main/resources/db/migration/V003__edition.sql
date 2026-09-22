create table series (
    id   uuid primary key,
    name text not null
);

create unique index series_name_unique on series (lower(name));

create table author (
    id   uuid primary key,
    name text not null
);

create unique index author_name_unique on author (lower(name));

create table edition (
    id               uuid primary key,
    isbn13           text unique,
    kind             text not null,
    title            text not null,
    subtitle         text,
    series_id        uuid references series (id),
    volume_number    int,
    collection       text,
    publisher        text,
    publication_year int,
    language         text,
    page_count       int,
    summary          text,
    cover_url        text
);

create table contribution (
    edition_id uuid not null references edition (id) on delete cascade,
    author_id  uuid not null references author (id),
    role       text not null,
    primary key (edition_id, author_id, role)
);
