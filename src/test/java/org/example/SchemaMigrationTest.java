package org.example;

import org.example.web.IT.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Extends the shared integration-test base: one container and one Spring
// context for the whole run (see AbstractIntegrationTest).
public class SchemaMigrationTest extends AbstractIntegrationTest {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void flywayCreatesTables() {
		List<String> tables = jdbcTemplate.queryForList(
				"SELECT table_name FROM information_schema.tables WHERE table_schema='public'",String.class);

		assertThat(tables).contains("publisher", "book", "author", "author_book");
	}
}
