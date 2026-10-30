package org.example.web;

import org.example.dto.AuthorRequest;
import org.example.dto.AuthorResponse;
import org.example.exception.ResourceNotFoundException;
import org.example.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AuthorController.class)
@Import(ApiExceptionHandler.class)
class AuthorControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockitoBean private AuthorService authorService;

	@Test
	void getAllAuthorsReturnsAuthor() throws Exception {
		UUID uuid = UUID.randomUUID();
		AuthorResponse response = new AuthorResponse(uuid, "John", "Doe", LocalDate.of(1990, 1, 1));

		when(authorService.getAllAuthors()).thenReturn(List.of(response));

		mockMvc.perform(get("/api/authors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(uuid.toString()))
				.andExpect(jsonPath("$[0].firstName").value("John"))
				.andExpect(jsonPath("$[0].lastName").value("Doe"))
				.andExpect(jsonPath("$[0].birthDate").value("1990-01-01"));
	}

	@Test
	void getAllAuthorsReturnsEmptyList() throws Exception {
		when(authorService.getAllAuthors()).thenReturn(List.of());

		mockMvc.perform(get("/api/authors"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void getAuthorByIdReturnsAuthor() throws Exception {
		UUID uuid = UUID.randomUUID();
		AuthorResponse response = new AuthorResponse(uuid, "John", "Doe", LocalDate.of(1990, 1, 1));

		when(authorService.getAuthorById(uuid)).thenReturn(response);

		mockMvc.perform(get("/api/authors/{id}", uuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(uuid.toString()))
				.andExpect(jsonPath("$.firstName").value("John"))
				.andExpect(jsonPath("$.lastName").value("Doe"));
	}

	@Test
	void getAuthorByIdReturnsNotFound() throws Exception {
		UUID uuid = UUID.randomUUID();
		when(authorService.getAuthorById(uuid)).thenThrow(new ResourceNotFoundException("Author not found, id: " + uuid));

		mockMvc.perform(get("/api/authors/{id}", uuid))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Author not found, id: " + uuid));
	}

	@Test
	void createAuthorReturnsCreatedAuthor() throws Exception {
		AuthorResponse response = new AuthorResponse(UUID.randomUUID(), "John", "Doe", LocalDate.of(1990, 1, 1));
		String requestJson =
				"""
				{"firstName":"John","lastName":"Doe","birthDate":"1990-01-01"}
				""";
		when(authorService.createAuthor(any(AuthorRequest.class))).thenReturn(response);

		mockMvc
				.perform(post("/api/authors").contentType("application/json").content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(response.id().toString()))
				.andExpect(jsonPath("$.firstName").value("John"))
				.andExpect(jsonPath("$.lastName").value("Doe"));
	}

	@Test
	void createAuthorBadRequest() throws Exception {
		String requestJson = "{}";

		mockMvc
				.perform(post("/api/authors").contentType("application/json").content(requestJson))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail", containsString("firstName: must not be blank")))
				.andExpect(jsonPath("$.detail", containsString("lastName: must not be blank")));

		verify(authorService, never()).createAuthor(any());
	}

	@Test
	void updateAuthorReturnsUpdatedAuthor() throws Exception {
		UUID id = UUID.randomUUID();
		AuthorResponse response = new AuthorResponse(id, "Jane", "Doe", LocalDate.of(1992, 2, 2));
		String requestJson =
				"""
				{"firstName":"Jane","lastName":"Doe","birthDate":"1992-02-02"}
				""";
		when(authorService.updateAuthor(eq(id), any(AuthorRequest.class))).thenReturn(response);

		mockMvc
				.perform(
						put("/api/authors/{id}", id).contentType("application/json").content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.firstName").value("Jane"))
				.andExpect(jsonPath("$.lastName").value("Doe"));
	}

	@Test
	void updateAuthorBadRequest() throws Exception {
		String requestJson = "{}";

		mockMvc
				.perform(put("/api/authors/{id}", UUID.randomUUID()).contentType("application/json").content(requestJson))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail", containsString("firstName: must not be blank")))
				.andExpect(jsonPath("$.detail", containsString("lastName: must not be blank")));

		verify(authorService, never()).updateAuthor(any(), any());
	}

	@Test
	void updateAuthorReturnsNotFound() throws Exception {
		UUID id = UUID.randomUUID();
		String requestJson =
				"""
				{"firstName":"Jane","lastName":"Doe","birthDate":"1992-02-02"}
				""";
		when(authorService.updateAuthor(eq(id), any(AuthorRequest.class))).
				thenThrow(new ResourceNotFoundException("Author not found, id: " + id));

		mockMvc
				.perform(
						put("/api/authors/{id}", id).contentType("application/json").content(requestJson))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Author not found, id: " + id));
	}

	@Test
	void deleteAuthorReturnsNoContent() throws Exception {
		UUID id = UUID.randomUUID();

		doNothing().when(authorService).deleteAuthor(id);

		mockMvc.perform(delete("/api/authors/{id}", id)).andExpect(status().isNoContent());

		verify(authorService).deleteAuthor(id);
	}

	@Test
	void deleteAuthorReturnsNotFound() throws Exception {
		UUID id = UUID.randomUUID();

		doThrow(new ResourceNotFoundException("Author not found, id: " + id))
				.when(authorService).deleteAuthor(id);

		mockMvc.perform(delete("/api/authors/{id}", id)).andExpect(status().isNotFound());
	}

}
