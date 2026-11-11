ALTER TABLE edition
    ALTER COLUMN search_vector SET EXPRESSION AS (
        TO_TSVECTOR('words_ignoring_case_and_accents'::regconfig, search_text)
            || CASE WHEN volume_number IS NULL THEN ''::tsvector ELSE ARRAY_TO_TSVECTOR(ARRAY['#' || volume_number]) END
    );
