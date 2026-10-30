package org.example.web.IT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
Verifies that the real PostgreSQL constraint names match the 409 mapping in
ApiExceptionHandler. The mocked tests in BookControllerTest check the mapping
itself; these tests check that the database actually reports those names.

The author-book 409 cannot be reached through the API single-threaded: the
service's idempotency guard makes a duplicate association a no-op (as plan.md
requires), so the constraint only fires under a race. The constraint and its
name are therefore verified directly against the database.
*/

class BookConflictIT extends AbstractIntegrationTest {

	@Autowired private MockMvc mockMvc;

	@Autowired private JdbcTemplate jdbcTemplate;

	@Test
	void createBookWithDuplicateIsbnReturnsConflict() throws Exception {
		createBook("isbn-dup-it-1").andExpect(status().isCreated());

		createBook("isbn-dup-it-1")
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("A book with this ISBN already exists"));
	}

	@Test
	void authorBookUniqueConstraintIsNamedUqAuthorBook() {
		UUID bookId = UUID.randomUUID();
		UUID authorId = UUID.randomUUID();
		seedBook(bookId);
		seedAuthor(authorId);

		insertAuthorBook(bookId, authorId);

		assertThatThrownBy(() -> insertAuthorBook(bookId, authorId))
				.isInstanceOf(DataIntegrityViolationException.class)
				.hasMessageContaining("uq_author_book");
	}

	// --- Helpers ---

	private ResultActions createBook(String isbn) throws Exception {
		String bookJson =
				"""
				{"title":"Conflict IT","subTitle":"s","description":"d","pages":10,"isbn":"%s"}
				""".formatted(isbn);
		return mockMvc.perform(
				post("/api/books").contentType(MediaType.APPLICATION_JSON).content(bookJson));
	}

	private void seedBook(UUID bookId) {
		jdbcTemplate.update(
				"INSERT INTO book (id, title, sub_title, pages) VALUES (?, ?, ?, ?)",
				bookId,
				"Conflict IT",
				"s",
				10);
	}

	private void seedAuthor(UUID authorId) {
		jdbcTemplate.update(
				"INSERT INTO author (id, first_name, last_name) VALUES (?, ?, ?)",
				authorId,
				"John",
				"Doe");
	}

	private void insertAuthorBook(UUID bookId, UUID authorId) {
		jdbcTemplate.update(
				"INSERT INTO author_book (id, author_id, book_id) VALUES (?, ?, ?)",
				UUID.randomUUID(),
				authorId,
				bookId);
	}
}
