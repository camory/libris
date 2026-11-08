DROP INDEX author_name_unique;

CREATE UNIQUE INDEX author_name_unique ON author ((name COLLATE ignoring_case_and_accents));
