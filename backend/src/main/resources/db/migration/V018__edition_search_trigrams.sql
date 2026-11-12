CREATE EXTENSION pg_trgm;

CREATE FUNCTION without_accents(text) RETURNS text
    IMMUTABLE PARALLEL SAFE STRICT
    RETURN UNACCENT('unaccent', $1);

CREATE INDEX edition_search_trigrams ON edition USING GIN (WITHOUT_ACCENTS(search_text) gin_trgm_ops);
