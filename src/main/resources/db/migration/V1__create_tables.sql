/*
Publisher → Book    | 1 : 0..N | A publisher can publish zero or many books. |
Book → Publisher    | 0..1 : 1 | A book has zero or one publisher. |
Author → Book       | N : M    | An author can write many books. |
Book → Author       | N : M    | A book can have many authors. |
*/

CREATE TABLE publisher (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

CREATE TABLE book (
  id UUID PRIMARY KEY,
  title VARCHAR(100) NOT NULL,
  sub_title VARCHAR(500) NOT NULL,
  description TEXT,
  pages INTEGER NOT NULL,
  isbn VARCHAR(20) UNIQUE,
  publisher_id UUID,

  CONSTRAINT fk_book_publisher
      FOREIGN KEY (publisher_id)
          REFERENCES publisher(id)
);

CREATE TABLE author (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_date DATE
);

CREATE TABLE author_book(
    id UUID PRIMARY KEY,
    author_id UUID NOT NULL,
    book_id   UUID NOT NULL,

    CONSTRAINT uq_author_book
        UNIQUE (author_id, book_id),

    CONSTRAINT fk_author_book_author
        FOREIGN KEY (author_id)
            REFERENCES author (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_author_book_book
        FOREIGN KEY (book_id)
            REFERENCES book (id)
            ON DELETE CASCADE
);