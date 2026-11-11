CREATE EXTENSION unaccent;

CREATE TEXT SEARCH CONFIGURATION words_ignoring_case_and_accents (COPY = simple);
ALTER TEXT SEARCH CONFIGURATION words_ignoring_case_and_accents
    ALTER MAPPING FOR word, hword, hword_part WITH unaccent, simple;

ALTER TABLE edition ADD COLUMN search_text text;

UPDATE edition
SET search_text = edition.title;

ALTER TABLE edition ALTER COLUMN search_text SET NOT NULL;

ALTER TABLE edition
    ADD COLUMN search_vector tsvector
        GENERATED ALWAYS AS (TO_TSVECTOR('words_ignoring_case_and_accents'::regconfig, search_text)) STORED;

CREATE INDEX edition_search_vector ON edition USING GIN (search_vector);
