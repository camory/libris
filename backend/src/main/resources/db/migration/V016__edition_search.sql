CREATE EXTENSION unaccent;

CREATE TEXT SEARCH CONFIGURATION words_ignoring_case_and_accents (COPY = simple);
ALTER TEXT SEARCH CONFIGURATION words_ignoring_case_and_accents
    ALTER MAPPING FOR asciiword, word, numword, asciihword, hword, numhword, hword_asciipart, hword_part,
                      hword_numpart, email, url, host, url_path, file, sfloat, float, int, uint, version
        WITH unaccent, simple;

ALTER TABLE edition ADD COLUMN search_text text;

UPDATE edition
SET search_text = CONCAT_WS(
      ' ',
      edition.title,
      edition.subtitle,
      (SELECT series.name FROM series WHERE series.id = edition.series_id),
      (SELECT STRING_AGG(author.name, ' ')
       FROM contribution
       JOIN author ON author.id = contribution.author_id
       WHERE contribution.edition_id = edition.id)
    );

ALTER TABLE edition ALTER COLUMN search_text SET NOT NULL;

ALTER TABLE edition
    ADD COLUMN search_vector tsvector
        GENERATED ALWAYS AS (TO_TSVECTOR('words_ignoring_case_and_accents'::regconfig, search_text)) STORED;

CREATE INDEX edition_search_vector ON edition USING GIN (search_vector);
