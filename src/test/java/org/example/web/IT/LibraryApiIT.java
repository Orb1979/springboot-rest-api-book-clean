package org.example.web.IT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
End-to-end coverage:
- SchemaMigrationTest: Flyway creates the schema
- BookConflictIT:      unique ISBN and unique author_book constraints
- PublisherDeleteIT:    ON DELETE SET NULL on fk_book_publisher

This class covers: full CRUD flows for all three resources over real HTTP,
the author_book ON DELETE CASCADE behaviour (book delete and author delete),
validation errors, and unknown-reference 404s.

All tests share one database (see AbstractIntegrationTest): fixtures are
marked with unique titles/isbns and counted by marker, never globally.
*/

class LibraryApiIT extends AbstractIntegrationTest {

	@Autowired private MockMvc mockMvc;

	@Autowired private JdbcTemplate jdbcTemplate;

	// --- Full CRUD flows ---

	@Test
	void publisherCrudFlow() throws Exception {
		String id = createPublisher("O'Reilly");

		getJson("/api/publishers/" + id)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("O'Reilly"));

		mockMvc
				.perform(
						put("/api/publishers/{id}", id)
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
								{"name":"O'Reilly Media"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("O'Reilly Media"));

		mockMvc.perform(delete("/api/publishers/{id}", id)).andExpect(status().isNoContent());

		getJson("/api/publishers/" + id).andExpect(status().isNotFound());
	}

	@Test
	void authorCrudFlow() throws Exception {
		String id = createAuthor("Joshua", "Bloch");

		getJson("/api/authors/" + id)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.firstName").value("Joshua"))
				.andExpect(jsonPath("$.lastName").value("Bloch"));

		mockMvc
				.perform(
						put("/api/authors/{id}", id)
								.contentType(MediaType.APPLICATION_JSON)
								.content("""
								{"firstName":"Joshua","lastName":"Bloch","birthDate":"1961-08-28"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.birthDate").value("1961-08-28"));

		mockMvc.perform(delete("/api/authors/{id}", id)).andExpect(status().isNoContent());

		getJson("/api/authors/" + id).andExpect(status().isNotFound());
	}

	@Test
	void bookCrudFlowWithPublisherAndAuthors() throws Exception {
		String publisherId = createPublisher("Manning");
		String authorId = createAuthor("Martin", "Fowler");

		String bookJson =
				("""
				{"title":"Refactoring","subTitle":"Improving the Design of Existing Code",\
				"description":"classic","pages":448,"isbn":"%s",\
				"publisherId":"%s","authorIds":["%s"]}
				""")
				.formatted(uniqueIsbn(), publisherId, authorId);

		String bookId = idOf(
				mockMvc
						.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(bookJson))
						.andExpect(status().isCreated())
						.andExpect(jsonPath("$.title").value("Refactoring"))
						.andExpect(jsonPath("$.publisher.name").value("Manning"))
						.andExpect(jsonPath("$.authors[0].lastName").value("Fowler")));

		getJson("/api/books/" + bookId)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.pages").value(448));

		String updateJson =
				("""
				{"title":"Refactoring","subTitle":"2nd Edition","description":"updated",\
				"pages":448,"isbn":"%s"}
				""")
				.formatted(uniqueIsbn());
		mockMvc
				.perform(put("/api/books/{id}", bookId).contentType(MediaType.APPLICATION_JSON).content(updateJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.subTitle").value("2nd Edition"))
				.andExpect(jsonPath("$.publisher.name").value("Manning"))
				.andExpect(jsonPath("$.authors[0].lastName").value("Fowler"));

		mockMvc.perform(delete("/api/books/{id}", bookId)).andExpect(status().isNoContent());

		getJson("/api/books/" + bookId).andExpect(status().isNotFound());
	}

	// --- author_book cascade behaviour ---

	@Test
	void deletingBookRemovesAuthorLinksButKeepsAuthor() throws Exception {
		String authorId = createAuthor("Robert", "Martin");
		String bookId = createBook("Clean Code");
		associate(bookId, authorId);

		assertThat(countLinks(bookId, authorId)).isEqualTo(1);

		mockMvc.perform(delete("/api/books/{id}", bookId)).andExpect(status().isNoContent());

		assertThat(countLinks(bookId, authorId)).isZero();
		getJson("/api/authors/" + authorId)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.lastName").value("Martin"));
	}

	@Test
	void deletingAuthorRemovesLinksButKeepsBook() throws Exception {
		String authorId = createAuthor("Kent", "Beck");
		String bookId = createBook("TDD by Example");
		associate(bookId, authorId);

		mockMvc.perform(delete("/api/authors/{id}", authorId)).andExpect(status().isNoContent());

		assertThat(countLinks(bookId, authorId)).isZero();
		getJson("/api/books/" + bookId)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("TDD by Example"))
				.andExpect(jsonPath("$.authors").isEmpty());
	}

	// --- Validation and unknown references ---

	@Test
	void validationErrorsReturnProblemDetail() throws Exception {
		String marker = "validation-marker-" + UUID.randomUUID();
		mockMvc
				.perform(
						post("/api/books")
								.contentType(MediaType.APPLICATION_JSON)
								.content(
										("""
										{"title":"","subTitle":"","pages":0,"description":"%s"}
										""")
										.formatted(marker)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.title").value("Bad Request"))
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("title: must not be blank")));

		assertThat(countBooksByTitle("")).isZero();
	}

	@Test
	void createBookWithUnknownPublisherReturnsNotFound() throws Exception {
		String bookJson =
				("""
				{"title":"Orphan %s","subTitle":"s","description":"d","pages":10,"isbn":"%s",\
				"publisherId":"%s"}
				""")
				.formatted(UUID.randomUUID(), uniqueIsbn(), UUID.randomUUID());

		mockMvc
				.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(bookJson))
				.andExpect(status().isNotFound());

		// The book was not persisted: no row carries this marker title.
		assertThat(countBooksLike("Orphan ")).isZero();
	}

	// --- Helpers ---

	private ResultActions getJson(String url) throws Exception {
		return mockMvc.perform(get(url));
	}

	private String createPublisher(String name) throws Exception {
		String response =
				mockMvc
						.perform(
								post("/api/publishers")
										.contentType(MediaType.APPLICATION_JSON)
										.content("""
										{"name":"%s"}
										""".formatted(name)))
						.andExpect(status().isCreated())
						.andReturn()
						.getResponse()
						.getContentAsString();
		return extractId(response);
	}

	private String createAuthor(String firstName, String lastName) throws Exception {
		String response =
				mockMvc
						.perform(
								post("/api/authors")
										.contentType(MediaType.APPLICATION_JSON)
										.content("""
										{"firstName":"%s","lastName":"%s"}
										""".formatted(firstName, lastName)))
						.andExpect(status().isCreated())
						.andReturn()
						.getResponse()
						.getContentAsString();
		return extractId(response);
	}

	private String createBook(String title) throws Exception {
		String bookJson =
				("""
				{"title":"%s","subTitle":"s","description":"d","pages":10,"isbn":"%s"}
				""")
				.formatted(title, uniqueIsbn());
		String response =
				mockMvc
						.perform(post("/api/books").contentType(MediaType.APPLICATION_JSON).content(bookJson))
						.andExpect(status().isCreated())
						.andReturn()
						.getResponse()
						.getContentAsString();
		return extractId(response);
	}

	private void associate(String bookId, String authorId) throws Exception {
		mockMvc
				.perform(put("/api/books/{id}/authors/{authorId}", bookId, authorId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.authors[0].id").value(authorId));
	}

	// The isbn column is varchar(20): "it-" + 8 hex chars stays well under the limit.
	private String uniqueIsbn() {
		return "it-" + UUID.randomUUID().toString().substring(0, 8);
	}

	// Minimal id extraction from a JSON response body; no Jackson dependency needed
	// on the test classpath.
	private String extractId(String responseBody) {
		var matcher = java.util.regex.Pattern
				.compile("\"id\"\\s*:\\s*\"([a-f0-9-]{36})\"")
				.matcher(responseBody);
		if (!matcher.find()) {
			throw new IllegalStateException("no id in response: " + responseBody);
		}
		return matcher.group(1);
	}

	private String idOf(ResultActions result) throws Exception {
		return extractId(result.andReturn().getResponse().getContentAsString());
	}

	private int countLinks(String bookId, String authorId) {
		return jdbcTemplate.queryForObject(
				"SELECT count(*) FROM author_book WHERE book_id = ? AND author_id = ?",
				Integer.class,
				UUID.fromString(bookId),
				UUID.fromString(authorId));
	}

	// Counted by title marker, not globally: the database is shared across all
	// integration-test classes (see AbstractIntegrationTest).
	private int countBooksByTitle(String title) {
		return jdbcTemplate.queryForObject(
				"SELECT count(*) FROM book WHERE title = ?", Integer.class, title);
	}

	private int countBooksLike(String titlePrefix) {
		return jdbcTemplate.queryForObject(
				"SELECT count(*) FROM book WHERE title LIKE ?", Integer.class, titlePrefix + "%");
	}
}
