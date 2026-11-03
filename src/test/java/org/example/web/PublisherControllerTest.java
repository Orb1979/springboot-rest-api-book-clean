package org.example.web;

import org.example.application.publisher.PublisherUseCase;
import org.example.application.publisher.PublisherRequest;
import org.example.application.publisher.PublisherResponse;
import org.example.application.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PublisherController.class)
@Import(ApiExceptionHandler.class)
class PublisherControllerTest {

	@Autowired private MockMvc mockMvc;

	@MockitoBean private PublisherUseCase publisherUseCase;

	@Test
	void getAllPublishersReturnsPublisher() throws Exception {
		UUID uuid = UUID.randomUUID();
		PublisherResponse response = new PublisherResponse(uuid, "goodreads");

		when(publisherUseCase.getAllPublishers()).thenReturn(List.of(response));

		mockMvc.perform(get("/api/publishers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(uuid.toString()))
				.andExpect(jsonPath("$[0].name").value("goodreads"));
	}

	@Test
	void getAllPublishersReturnsEmptyList() throws Exception {
		when(publisherUseCase.getAllPublishers()).thenReturn(List.of());

		mockMvc.perform(get("/api/publishers"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void getPublishersByIdReturnsPublisher() throws Exception {
		UUID uuid = UUID.randomUUID();
		PublisherResponse response = new PublisherResponse(uuid, "goodreads");

		when(publisherUseCase.getPublisherById(uuid)).thenReturn(response);

		mockMvc.perform(get("/api/publishers/{id}", uuid))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(uuid.toString()))
				.andExpect(jsonPath("$.name").value("goodreads"));
	}

	@Test
	void getPublishersByIdReturnsNotFound() throws Exception {
		UUID uuid = UUID.randomUUID();
		when(publisherUseCase.getPublisherById(uuid)).thenThrow(new ResourceNotFoundException("Publisher not found, id: " + uuid));

		mockMvc.perform(get("/api/publishers/{id}", uuid))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Publisher not found, id: " + uuid));
	}

	@Test
	void createPublisherReturnsCreatedPublisher() throws Exception {
		PublisherResponse response = new PublisherResponse(UUID.randomUUID(), "goodreads");
		String requestJson =
				"""
				{"name":"goodreads"}
				""";
		when(publisherUseCase.createPublisher(any(PublisherRequest.class))).thenReturn(response);

		mockMvc
				.perform(post("/api/publishers").contentType("application/json").content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(response.id().toString()))
				.andExpect(jsonPath("$.name").value("goodreads"));
	}

	@Test
	void createPublisherBadRequest() throws Exception {
		String requestJson = "{}";

		mockMvc
				.perform(post("/api/publishers").contentType("application/json").content(requestJson))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("name: must not be blank"));

		verify(publisherUseCase, never()).createPublisher(any());
	}

	@Test
	void updatePublisherReturnsUpdatedPublisher() throws Exception {
		UUID id = UUID.randomUUID();
		PublisherResponse response = new PublisherResponse(id, "No Starch");
		String requestJson =
				"""
				{"name":"No Starch"}
				""";
		when(publisherUseCase.updatePublisher(eq(id), any(PublisherRequest.class))).thenReturn(response);

		mockMvc
				.perform(
						put("/api/publishers/{id}", id).contentType("application/json").content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id.toString()))
				.andExpect(jsonPath("$.name").value("No Starch"));
	}

	@Test
	void updatePublisherBadRequest() throws Exception {
		String requestJson = "{}";

		mockMvc
				.perform(put("/api/publishers/{id}", UUID.randomUUID()).contentType("application/json").content(requestJson))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("name: must not be blank"));

		verify(publisherUseCase, never()).createPublisher(any());
	}

	@Test
	void updatePublisherReturnsNotFound() throws Exception {
		UUID id = UUID.randomUUID();
		String requestJson =
				"""
				{"name":"No Starch"}
				""";
		when(publisherUseCase.updatePublisher(eq(id), any(PublisherRequest.class))).thenThrow(new ResourceNotFoundException("Publisher not found, id: " + id));

		mockMvc
				.perform(
						put("/api/publishers/{id}", id).contentType("application/json").content(requestJson))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Publisher not found, id: " + id));
	}

	@Test
	void deletePublisherReturnsNoContent() throws Exception {
		UUID id = UUID.randomUUID();

		doNothing().when(publisherUseCase).deletePublisher(id);

		mockMvc.perform(delete("/api/publishers/{id}", id)).andExpect(status().isNoContent());

		verify(publisherUseCase).deletePublisher(id);
	}

	@Test
	void deletePublisherReturnsNotFound() throws Exception {
		UUID id = UUID.randomUUID();

		doThrow(new ResourceNotFoundException("Publisher not found, id: " + id))
				.when(publisherUseCase).deletePublisher(id);

		mockMvc.perform(delete("/api/publishers/{id}", id)).andExpect(status().isNotFound());
	}

}