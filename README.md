# Rest api books

A small library catalogue REST API — publishers, authors, and books \
Flyway migrations, and testing (JUnit 5, AssertJ, Mockito, `@WebMvcTest`/`@DataJpaTest` slices).

## Build & run

```bash
./gradlew build   # compiles and runs the test suite
./gradlew bootRun # needs a PostgreSQL at localhost:5432 (see application.properties)
```

The schema is managed by Flyway (`src/main/resources/db/migration`), not by Hibernate
(`spring.jpa.hibernate.ddl-auto=validate`).

## Data model

| Relationship | Cardinality | Implementation |
|---|---|---|
| Publisher → Book | 1 : 0..N | `book.publisher_id` FK, `ON DELETE SET NULL` |
| Author ↔ Book | N : M | `author_book` join table, mapped as its own `AuthorBook` entity |

The author–book link is mapped as a **join entity** instead of `@ManyToMany` see [Design notes](#design-notes).

Deleting a publisher does **not** delete or block its books: the foreign key is declared
`ON DELETE SET NULL` (`V2__publisher_on_delete_set_null.sql`), so the books survive and
simply become unassigned (`"publisher": null`). See [Deleting a publisher](#deleting-a-publisher).

## API overview

Base URL: `http://localhost:8080`, all bodies are JSON. IDs are UUIDs.

| Resource | Method | Path | Success |
|---|---|---|---|
| Publisher | GET | `/api/publishers` | 200, list |
| Publisher | GET | `/api/publishers/{id}` | 200 |
| Publisher | POST | `/api/publishers` | 201 |
| Publisher | PUT | `/api/publishers/{id}` | 200 |
| Publisher | DELETE | `/api/publishers/{id}` | 204 |
| Author | GET | `/api/authors` | 200, list |
| Author | GET | `/api/authors/{id}` | 200 |
| Author | POST | `/api/authors` | 201 |
| Author | PUT | `/api/authors/{id}` | 200 |
| Author | DELETE | `/api/authors/{id}` | 204 |
| Book | GET | `/api/books` | 200, list |
| Book | GET | `/api/books/{id}` | 200 |
| Book | POST | `/api/books` | 201 |
| Book | PUT | `/api/books/{id}` | 200 |
| Book | DELETE | `/api/books/{id}` | 204 |
| Book | PUT | `/api/books/{id}/authors/{authorId}` | 200, updated book |
| Book | DELETE | `/api/books/{id}/authors/{authorId}` | 200, updated book |
| Book | DELETE | `/api/books/{id}/publisher` | 200, updated book |

### Requests

```json
// POST/PUT /api/publishers
{ "name": "Manning" }

// POST/PUT /api/authors
{
  "firstName": "Joshua",
  "lastName": "Bloch",
  "birthDate": "1961-08-28"
}

// POST/PUT /api/books
{
  "title": "Effective Java",
  "subTitle": "Best Practices for the Java Platform",
  "description": "…",
  "pages": 416,
  "isbn": "978-0134685991",
  "publisherId": "…uuid…",
  "authorIds": ["…uuid…", "…uuid…"]
}
```

Validation rules, per field:
- **`name`** (publisher) — required, max 100 chars
- **`firstName`** (author) — required, max 100 chars
- **`lastName`** (author) — required, max 100 chars
- **`birthDate`** (author) — optional, must be in the past (`@Past`)
- **`title`** (book) — required, max 255 chars
- **`subTitle`** (book) — required, max 500 chars
- **`description`** (book) — optional, max 5000 chars
- **`pages`** (book) — required, must be greater than 0 (`@Positive`)
- **`isbn`** (book) — optional, max 20 chars, unique in the database (a duplicate returns 409)
- **`publisherId`** (book) — optional UUID; when present it must reference an existing publisher (checked in the service)
- **`authorIds`** (book) — optional list of UUIDs; when present every entry must reference an existing author (checked in the service, in one batch)

Bean-validation violations return 400 (see Errors); unknown `publisherId`/`authorIds` return 404.

### Responses

```json
// GET /api/books/{id}
{
  "id": "…uuid…",
  "title": "Effective Java",
  "subTitle": "Best Practices for the Java Platform",
  "description": "…",
  "pages": 416,
  "isbn": "978-0134685991",
  "publisher": { "id": "…uuid…", "name": "Manning" },
  "authors": [
    { "id": "…uuid…", "firstName": "Joshua", "lastName": "Bloch", "birthDate": "1961-08-28" }
  ]
}
```

`publisher` is `null` when the book has none; `authors` is `[]` when the book has none.

### Errors

Errors use RFC 7807 `ProblemDetail`:

```json
// 404
{ "type": "about:blank", "title": "Not Found", "status": 404, "detail": "Book not found, id: …", "instance": "/api/books/…" }

// 400 — bean validation failures are joined into detail
{ "type": "about:blank", "title": "Bad Request", "status": 400, "detail": "title: must not be blank", "instance": "/api/books" }

// 409 — database unique constraint violations
{ "type": "about:blank", "title": "Conflict", "status": 409, "detail": "A book with this ISBN already exists", "instance": "/api/books" }
```

**409 Conflict** covers constraint violations detected by the database: a duplicate
ISBN on `book`, and a duplicate author–book link in `author_book` (possible only under
concurrent requests — single-threaded, associating the same author twice is an
idempotent no-op). Unknown or unrelated constraint violations return a generic
409 message without leaking schema details.

## Deleting a publisher

`DELETE /api/publishers/{id}` returns `204 No Content` even when the publisher still has
books. The books are **not** deleted and the request is **not** rejected — they are
simply unassigned:

```
before                                    after DELETE /api/publishers/{publisherId}
──────                                    ───────────────────────────────────────────
book "Effective Java"  publisher_id: P    book "Effective Java"  publisher_id: NULL
publisher P            name: Manning      (publisher P no longer exists)
```

This is enforced by the database, not by application code: `V2__publisher_on_delete_set_null.sql`
recreates `fk_book_publisher` with `ON DELETE SET NULL`. `PublisherService.deletePublisher`
therefore stays a plain existence check + delete, and Hibernate's `ddl-auto=validate`
is unaffected (it validates tables and columns, not FK actions).

Consequences worth remembering:

- A book may end up with no publisher through no action of its own — `publisher` in the
  response is `null`, which is the same representation as a book that never had one.
- To keep books assigned, reassign them (`PUT /api/books/{id}` with a new `publisherId`)
  before deleting the publisher.
- Only `DELETE /api/publishers/{id}` triggers this. `DELETE /api/books/{id}` is unrelated
  and deletes the book (and its author links, via `ON DELETE CASCADE` on `author_book`).

## Update semantics: omitted vs. empty vs. explicit

`PUT` is defined by RFC 9110 as a *full replacement* of the representation. \
This API therefore deviates from this in regard when updating books with authors

| Request field | Omitted / `null` | Empty (`[]`) | Explicit value |
|---|---|---|---|
| Scalar fields (`title`, `subTitle`, `description`, `pages`, `isbn`) | overwritten | — | overwritten |
| `publisherId` | **keep current** | — | replace (404 if unknown) |
| `authorIds` | **keep current** | **clear all** | full replace (404 if any unknown) |


## Design notes

- **Join entity, not `@ManyToMany`.** The `author_book` link table is mapped as an `AuthorBook` entity  
  with two `@ManyToOne`s.
- **Cascades are narrow on purpose.** `Book.authorBooks` cascades `ALL` + `orphanRemoval`.  deleting a book
  deletes *link rows*, never  the authors. Removing an `AuthorBook` from the list deletes exactly that row.
- **`@Transactional` sits on the service methods** that mutate relationships, so a rejected update rolls back.
- **`BookService.replaceAuthors(bookId, authorIds)`** exposes the same author-set replacement as PUT /api/books/{id},
  it rejects a `null` list with 400 because "explicit list only" is its entire contract.

## Why the author–book link is its own entity (in plain words)

### The problem: how do you store "who wrote what"?

An author can write many books; a book can have many authors (N:M). Neither table can
hold that as a column, so the database uses a **third, middle table** that just pairs
them up:

```
author               author_book                book
──────               ──────────                ────
id        ◄────────  author_id
                      book_id   ─────────────► id
```

Each row in `author_book` says exactly one thing: *"this author is connected to this book."*

### Two ways to tell JPA about that middle table

Option 1 — `@ManyToMany` (the "hidden" way):

```java
@ManyToMany
private List<Author> authors; // middle table is invisible
```

Nice and short — but the middle table is *frozen*: it can only ever be
`(author_id, book_id)`, because JPA owns it and you never see it.

Option 2 — join entity (what this project does):

```java
@OneToMany(mappedBy = "book")
private List<AuthorBook> authorBooks;
```

`AuthorBook` is a normal `@Entity` — a real Java class with its own row in the
database, containing `author` and `book` fields. One extra class, one extra hop when
reading.



### The one-sentence version

> We made the author–book link a real entity instead of a hidden `@ManyToMany`, so that
> if we ever want to store data *about a specific author–book pair* (order,
> contribution), we just add a column — instead of a painful redesign.