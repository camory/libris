INSERT INTO awaited_cover (isbn13)
SELECT edition.isbn13
FROM edition
WHERE edition.isbn13 IS NOT NULL
  AND edition.cover_name IS NULL
  AND NOT EXISTS (
    SELECT 1
    FROM awaited_cover
    WHERE awaited_cover.isbn13 = edition.isbn13
  );
