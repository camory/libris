create table bookshelf (
    id   uuid primary key,
    name text not null
);

create table bookshelf_member (
    bookshelf_id uuid not null references bookshelf (id) on delete cascade,
    reader_id    uuid not null references reader (id),
    role         text not null check (role in ('OWNER', 'VIEWER')),
    primary key (bookshelf_id, reader_id)
);

alter table reader
    add column default_bookshelf_id uuid not null references bookshelf (id) deferrable initially deferred;
