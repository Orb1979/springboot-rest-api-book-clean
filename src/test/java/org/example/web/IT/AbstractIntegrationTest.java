package org.example.web.IT;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

/*
Shared infrastructure for the integration tests: one PostgreSQL container and
one Spring context per test run, instead of one of each per test class.

The container is started in a static initializer, so it is loaded exactly once per JVM and
shared by all subclasses. @ServiceConnection wires the datasource to the container
automatically. Because every subclass declares the same context configuration
(@SpringBootTest + @AutoConfigureMockMvc + this one service connection),
Spring's context caching boots the application once for the whole build.
*/

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

	@ServiceConnection
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

	static {
		postgres.start();
	}


// with ServiceConnection this is no longer needed:
// the @DynamicPropertySource would register with the highest precedence in the Environment
// it was overwriting the spring.datasource properties with the values of the running Testcontainers
// @ServiceConnection replaces this: Boot builds the DataSource from the container's JdbcConnectionDetails instead.

//	@DynamicPropertySource
//	static void datasourceProperties(DynamicPropertyRegistry registry) {
//		registry.add("spring.datasource.url", postgres::getJdbcUrl);
//		registry.add("spring.datasource.username", postgres::getUsername);
//		registry.add("spring.datasource.password", postgres::getPassword);
//	}
}