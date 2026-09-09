create table member (
    id uuid primary key,
    username text not null unique,
    display_name text not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);
