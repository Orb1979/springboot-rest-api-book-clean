package org.example.service;

import org.example.dto.PublisherRequest;
import org.example.dto.PublisherResponse;
import org.example.entity.Publisher;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.PublisherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublisherServiceTest {

	@Mock	private PublisherRepository publisherRepository;

	@InjectMocks private PublisherService publisherService;


	@Test
	void getAllPublishersReturnsPublishers() {
		Publisher publisher1 = new Publisher();
		publisher1.setId(UUID.randomUUID());
		publisher1.setName("Test Publisher 1");

		Publisher publisher2 = new Publisher();
		publisher2.setId(UUID.randomUUID());
		publisher2.setName("Test Publisher 2");

		when(publisherRepository.findAll()).thenReturn(List.of(publisher1, publisher2));

		List<PublisherResponse> result = publisherService.getAllPublishers();
		assertEquals(2, result.size());
		assertEquals(publisher1.getId(), result.get(0).id());
		assertEquals(publisher1.getName(), result.get(0).name());
		assertEquals(publisher2.getId(), result.get(1).id());
		assertEquals(publisher2.getName(), result.get(1).name());
	}

	@Test
	void getAllPublishersReturnsEmptyList() {
		when(publisherRepository.findAll()).thenReturn(List.of());

		List<PublisherResponse> result = publisherService.getAllPublishers();

		assertEquals(0, result.size());
	}

	@Test
	void getPublisherByIdReturnsPublisher() {
		UUID id = UUID.randomUUID();
		Publisher publisher = new Publisher();
		publisher.setId(id);
		publisher.setName("Manning");

		when(publisherRepository.findById(id)).thenReturn(Optional.of(publisher));

		PublisherResponse result = publisherService.getPublisherById(id);

		assertEquals(id, result.id());
		assertEquals("Manning", result.name());
	}

	@Test
	void getPublisherByIdReturnsNotFound() {
		UUID id = UUID.randomUUID();
		when(publisherRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> publisherService.getPublisherById(id));
	}

	@Test
	void createPublisherReturnsCreatedPublisher() {
		PublisherRequest requestDto = new PublisherRequest("publisherName");
		Publisher publisher = new Publisher();
		publisher.setId(UUID.randomUUID());
		publisher.setName("publisherName");

		when(publisherRepository.save(any(Publisher.class))).thenReturn(publisher);

		PublisherResponse result = publisherService.createPublisher(requestDto);

		assertEquals(publisher.getId(), result.id());
		assertEquals("publisherName", result.name());

		verify(publisherRepository).save(any(Publisher.class));
	}

	@Test
	void updatePublisherReturnsUpdatedPublisher() {
		UUID id = UUID.randomUUID();
		Publisher existingPublisher = new Publisher();
		existingPublisher.setId(id);
		existingPublisher.setName("Old Name");

		PublisherRequest requestDto = new PublisherRequest("New Name");

		when(publisherRepository.findById(id)).thenReturn(Optional.of(existingPublisher));
		when(publisherRepository.save(existingPublisher)).thenReturn(existingPublisher);

		PublisherResponse result = publisherService.updatePublisher(id, requestDto);

		assertEquals(id, result.id());
		assertEquals("New Name", result.name());
		verify(publisherRepository).save(existingPublisher);
	}

	@Test
	void updatePublisherReturnsNotFound() {
		UUID id = UUID.randomUUID();
		when(publisherRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> publisherService.updatePublisher(id, new PublisherRequest("a")));
		verify(publisherRepository, never()).save(any());
	}

	@Test
	void deletePublisherReturnsOk() {
		UUID id = UUID.randomUUID();
		when(publisherRepository.existsById(id)).thenReturn(true);

		publisherService.deletePublisher(id);

		verify(publisherRepository).deleteById(id);
	}

	@Test
	void deletePublisherReturnsNotFound() {
		UUID id = UUID.randomUUID();
		when(publisherRepository.existsById(id)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> publisherService.deletePublisher(id));
		verify(publisherRepository, never()).deleteById(any());
	}
}