create table member (
    id uuid primary key,
    username text not null unique,
    display_name text not null
);
