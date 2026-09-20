create table bookshelf (
    id   uuid primary key,
    name text not null
);

alter table reader
    add column default_bookshelf_id uuid not null references bookshelf (id);

create table membership (
    bookshelf_id uuid not null references bookshelf (id) on delete cascade,
    reader_id    uuid not null references reader (id) deferrable initially deferred,
    role         text not null,
    primary key (bookshelf_id, reader_id)
);
