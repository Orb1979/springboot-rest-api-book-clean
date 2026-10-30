package org.example.web.IT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
Verifies the ON DELETE SET NULL behaviour of fk_book_publisher
(V2__publisher_on_delete_set_null.sql) against real PostgreSQL.

This cannot be covered by a unit or @WebMvcTest slice: the unassignment is
performed by the database, and the service stays a plain existence check plus
delete, so a mocked test would only confirm the mock was called.
*/

class PublisherDeleteIT extends AbstractIntegrationTest {

	@Autowired private MockMvc mockMvc;

	@Autowired private JdbcTemplate jdbcTemplate;

	@Test
	void deletingPublisherUnassignsItsBooks() throws Exception {
		UUID publisherId = UUID.randomUUID();
		UUID bookId = UUID.randomUUID();
		seedPublisher(publisherId);
		seedBook(bookId, publisherId);

		mockMvc
				.perform(delete("/api/publishers/{id}", publisherId))
				.andExpect(status().isNoContent());

		assertThat(countPublishers(publisherId)).isZero();
		assertThat(countBooks(bookId)).isEqualTo(1);
		assertThat(publisherIdOf(bookId)).isNull();

		mockMvc
				.perform(get("/api/books/{id}", bookId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(bookId.toString()))
				.andExpect(jsonPath("$.publisher").value(nullValue()));
	}

	@Test
	void deletingPublisherInUseReturnsNoContentAndKeepsOtherBooks() throws Exception {
		UUID publisherId = UUID.randomUUID();
		UUID firstBookId = UUID.randomUUID();
		UUID secondBookId = UUID.randomUUID();
		seedPublisher(publisherId);
		seedBook(firstBookId, publisherId);
		seedBook(secondBookId, publisherId);

		mockMvc
				.perform(delete("/api/publishers/{id}", publisherId))
				.andExpect(status().isNoContent());

		assertThat(countBooks(firstBookId)).isEqualTo(1);
		assertThat(countBooks(secondBookId)).isEqualTo(1);
		assertThat(publisherIdOf(firstBookId)).isNull();
		assertThat(publisherIdOf(secondBookId)).isNull();
	}

	// --- Helpers ---

	private void seedPublisher(UUID publisherId) {
		jdbcTemplate.update("INSERT INTO publisher (id, name) VALUES (?, ?)", publisherId, "Manning");
	}

	private void seedBook(UUID bookId, UUID publisherId) {
		jdbcTemplate.update(
				"INSERT INTO book (id, title, sub_title, pages, publisher_id) VALUES (?, ?, ?, ?, ?)",
				bookId,
				"Effective Java",
				"Best Practices",
				416,
				publisherId);
	}

	private int countPublishers(UUID publisherId) {
		return jdbcTemplate.queryForObject(
				"SELECT count(*) FROM publisher WHERE id = ?", Integer.class, publisherId);
	}

	private int countBooks(UUID bookId) {
		return jdbcTemplate.queryForObject(
				"SELECT count(*) FROM book WHERE id = ?", Integer.class, bookId);
	}

	private UUID publisherIdOf(UUID bookId) {
		return jdbcTemplate.queryForObject(
				"SELECT publisher_id FROM book WHERE id = ?", UUID.class, bookId);
	}
}
