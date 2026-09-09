create table reader (
    id           uuid primary key,
    username     text not null unique,
    email        text not null,
    display_name text not null
);
