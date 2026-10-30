# Plan: Basic Book Library REST API

## 1. Goal

Build a basic REST API for managing publishers, books, and authors.

The API should support CRUD operations, allow books to be assigned to publishers, and allow multiple authors to be associated with a book through the `author_book` join table.

Keep the implementation simple, maintainable, and easy to test.

## 2. Technology stack

- Java 21
- Spring Boot 4
- Gradle
- Spring Web MVC
- Spring Data JPA
- Jakarta Bean Validation
- PostgreSQL
- Flyway for database migrations
- JUnit 5, Mockito, and Spring Boot Test
- Testcontainers for PostgreSQL integration tests

## 3. Database schema

Use the supplied SQL schema without changing its structure.

Tables:
- `publisher`: publisher information
- `book`: book information and optional publisher relationship
- `author`: author information
- `author_book`: many-to-many relationship between authors and books

Create the schema in a Flyway migration, for example `V1__create_library_schema.sql`.

Use UUID identifiers and map PostgreSQL `DATE` to Java `LocalDate`.

## 4. REST endpoints

### Publishers

- `POST /api/publishers` — create a publisher
- `GET /api/publishers` — list publishers
- `GET /api/publishers/{id}` — get a publisher
- `PUT /api/publishers/{id}` — update a publisher
- `DELETE /api/publishers/{id}` — delete a publisher

### Books

- `POST /api/books` — create a book
- `GET /api/books` — list books
- `GET /api/books/{id}` — get a book
- `PUT /api/books/{id}` — update a book
- `DELETE /api/books/{id}` — delete a book

A book response should include its publisher information and its authors.

### Authors

- `POST /api/authors` — create an author
- `GET /api/authors` — list authors
- `GET /api/authors/{id}` — get an author
- `PUT /api/authors/{id}` — update an author
- `DELETE /api/authors/{id}` — delete an author

### Book-author relationships

- `PUT /api/books/{bookId}/authors/{authorId}` — associate an author with a book
- `DELETE /api/books/{bookId}/authors/{authorId}` — remove an author from a book

The association endpoint should be idempotent: associating the same author twice must not create duplicate relationships.

## 5. Suggested package structure

com.example.library
├── dto
│   ├── PublisherRequest.java
│   ├── PublisherResponse.java
│   ├── BookRequest.java
│   ├── BookResponse.java
│   ├── AuthorRequest.java
│   └── AuthorResponse.java
├── entity
│   ├── Publisher.java
│   ├── Book.java
│   ├── Author.java
│   └── AuthorBook.java
├── repository
│   ├── PublisherRepository.java
│   ├── BookRepository.java
│   ├── AuthorRepository.java
│   └── AuthorBookRepository.java
├── service
│   ├── PublisherService.java
│   ├── BookService.java
│   └── AuthorService.java
├── web
│   ├── PublisherController.java
│   ├── BookController.java
│   ├── AuthorController.java
│   └── GlobalExceptionHandler.java
└── LibraryApplication.java

Keep each feature's code together instead of creating large, generic packages for every controller, service, and repository.

## 6. Implementation phases

### Phase 1 — Project setup and database

- Create the Spring Boot project and configure dependencies.
- Configure the PostgreSQL connection.
- Add the Flyway migration containing the supplied schema.
- Configure Hibernate with `ddl-auto=validate`.
- Verify that the application starts and Flyway creates the tables.

**Acceptance criteria:** The application starts successfully against an empty PostgreSQL database, and the database schema matches the supplied SQL.

### Phase 2 — Publisher CRUD

- Implement the Publisher entity, repository, service, controller, and DTOs.
- Implement all five publisher endpoints.
- Validate required fields.
- Return appropriate HTTP status codes.

**Acceptance criteria:** Publishers can be created, retrieved, updated, and deleted. Missing publishers return `404 Not Found`.

### Phase 3 — Author CRUD

- Implement the Author entity, repository, service, controller, and DTOs.
- Map `birth_date` to `LocalDate`.
- Implement all five author endpoints.
- Add validation for first and last names.

**Acceptance criteria:** Author CRUD works, including nullable birth dates.

### Phase 4 — Book CRUD and publisher relationship

- Implement the Book entity and repository.
- Map the optional many-to-one relationship to Publisher.
- Implement book request and response DTOs.
- Support creating and updating a book with an optional `publisherId`.
- Validate title, subtitle, and page count.
- Handle duplicate ISBN values.

**Acceptance criteria:** Books can be managed independently or assigned to an existing publisher. Invalid publisher IDs return `404`, and duplicate ISBN values return `409 Conflict`.

### Phase 5 — Author-book relationship

- Map the `author_book` table as an explicit `AuthorBook` entity.
- Use a UUID primary key and a unique constraint on `(author_id, book_id)`.
- Implement the endpoints for associating and disassociating authors.
- Verify that both referenced records exist before creating a relationship.
- Include authors in book responses without exposing JPA entities directly.

**Acceptance criteria:** A book can have multiple authors, and an author can have multiple books. Duplicate relationships are prevented.

### Phase 6 — Error handling and validation

- Add a global exception handler using `@RestControllerAdvice`.
- Return consistent error responses.
- Handle validation errors, missing resources, duplicate ISBNs, and database constraint violations.
- Use `@Valid` on request DTOs.

Suggested HTTP statuses:
- `200 OK` — successful reads and updates
- `201 Created` — successful creation
- `204 No Content` — successful deletion
- `400 Bad Request` — invalid input
- `404 Not Found` — resource does not exist
- `409 Conflict` — duplicate ISBN or another resource conflict

### Phase 7 — Testing

Write unit tests for service-layer business logic and integration tests against real PostgreSQL.

Unit tests:
- Publisher and author CRUD logic
- Book creation and publisher assignment
- Invalid references
- Author-book association rules

Integration tests:
- CRUD endpoints
- Flyway schema initialization
- JPA relationship mappings
- Unique ISBN constraint
- Unique author-book constraint
- Foreign key behavior
- HTTP status codes and validation errors

Use Testcontainers so database behavior is tested against PostgreSQL rather than an in-memory substitute.

### Phase 8 — Documentation and final verification

- Add a README with setup instructions.
- Document endpoints and example JSON requests.
- Include commands for running tests and starting the application.
- Verify the application from a REST client or curl.

## 7. Design rules

- Keep controllers thin; business logic belongs in services.
- Use DTOs for API requests and responses.
- Do not return JPA entities directly.
- Use constructor injection.
- Let the database generate neither UUIDs nor schema changes implicitly; generate UUIDs in the application and manage schema changes through Flyway.
- Use transactions for operations that update multiple records.
- Avoid adding pagination, authentication, caching, messaging, or complex abstractions in the initial version.

## 8. Definition of done

The project is complete when:
- All CRUD endpoints work.
- Books can reference publishers.
- Books and authors can be associated in both directions.
- Validation and error responses are consistent.
- Flyway manages the database schema.
- Integration tests pass against PostgreSQL.
- The README explains how to run and test the application.

## Database Relationships

| Relationship | Cardinality | Explanation |
|---|---|---|
| Publisher → Book | 1 : 0..N | One publisher can publish zero or many books. |
| Book → Publisher | 1 : 0..1 | One book can have zero or one publisher. |
| Author → Book | 1 : 0..N | One author can write zero or many books. |
| Book → Author | 1 : 1..N | One book can have one or many authors. |

**Author ↔ Book (Many-to-Many):**
```
Author  1 : 0..N AuthorBook 0..N : 1 Book
Author  1 : 0..N AuthorBook | One author can have zero or many AuthorBook records.
Book    1 : 0..N AuthorBook | One book can have zero or many AuthorBook records.
```
**Result:** Author and Book have a many-to-many relationship, implemented through the `author_book` join table.

### Constraints

- `book.publisher_id` is nullable, so a book can exist without a publisher.
- `author_book.author_id` and `author_book.book_id` are mandatory foreign keys.
- `UNIQUE (author_id, book_id)` prevents duplicate author-book relationships.
- Deleting an author or book cascades to its associated `author_book` records.

## reading cardinality

Each cardinality is written next to the entity whose instances are being counted \
it describes how many of those instances can be associated with ***one*** instance of the entity at the ***opposite end***.

Publisher  1 ─── 0..N  Book     | One publisher has ***zero or many*** books. Each book has exactly ***one*** Publisher \
Book       0..N ─── 1 Publisher | One book has exactly ***one*** Publisher. Each Publisher has ***zero or many books***




