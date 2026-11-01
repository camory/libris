DROP INDEX series_name_unique;

CREATE UNIQUE INDEX series_name_unique ON series (name COLLATE ignoring_case_and_accents);
