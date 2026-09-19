create table bookshelf (
    id   uuid primary key,
    name text not null
);

alter table reader
    add column default_bookshelf_id uuid not null references bookshelf (id);

create table reader_bookshelf (
    reader_id    uuid not null references reader (id) on delete cascade,
    bookshelf_id uuid not null references bookshelf (id),
    role         text not null check (role in ('OWNER', 'VIEWER')),
    primary key (reader_id, bookshelf_id)
);
