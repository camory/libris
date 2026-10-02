CREATE TABLE awaited_cover (
    isbn13 text PRIMARY KEY REFERENCES edition (isbn13) ON DELETE CASCADE,
    source text
);
