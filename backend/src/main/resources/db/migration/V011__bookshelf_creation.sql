ALTER TABLE bookshelf ADD COLUMN created_by uuid REFERENCES reader (id);
ALTER TABLE bookshelf ADD COLUMN created_at timestamptz;

UPDATE bookshelf
SET created_by = (
      SELECT membership.reader_id
      FROM membership
      WHERE membership.bookshelf_id = bookshelf.id
        AND membership.role = 'OWNER'
      ORDER BY membership.reader_id
      LIMIT 1
    ),
    created_at = now();

ALTER TABLE bookshelf ALTER COLUMN created_by SET NOT NULL;
ALTER TABLE bookshelf ALTER COLUMN created_at SET NOT NULL;
