ALTER TABLE copy ADD COLUMN added_by uuid REFERENCES reader (id);
ALTER TABLE copy ADD COLUMN added_at timestamptz;

UPDATE copy
SET added_by = (
      SELECT membership.reader_id
      FROM membership
      WHERE membership.bookshelf_id = copy.bookshelf_id
        AND membership.role = 'OWNER'
      ORDER BY membership.reader_id
      LIMIT 1
    ),
    added_at = now();

ALTER TABLE copy ALTER COLUMN added_by SET NOT NULL;
ALTER TABLE copy ALTER COLUMN added_at SET NOT NULL;
