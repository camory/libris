-- D03: both extensions are trusted, so the application user needs no superuser.
-- Never edit this file once merged: Flyway checksums it against the shared database.
CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
