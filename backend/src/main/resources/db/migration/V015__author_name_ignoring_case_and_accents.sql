DROP INDEX author_name_unique;

CREATE UNIQUE INDEX author_name_unique ON author ((BTRIM(name)) COLLATE ignoring_case_and_accents);
