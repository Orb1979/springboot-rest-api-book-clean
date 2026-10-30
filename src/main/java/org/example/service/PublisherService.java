package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.PublisherRequest;
import org.example.dto.PublisherResponse;
import org.example.entity.Publisher;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.PublisherRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublisherService {
	private final PublisherRepository publisherRepository;

	public List<PublisherResponse> getAllPublishers() {
		return publisherRepository.findAll().stream().map(PublisherService::toDto).toList();
	}

	public PublisherResponse getPublisherById(UUID id) {
		return publisherRepository.findById(id).map(PublisherService::toDto).orElseThrow(
				() -> new ResourceNotFoundException("Publisher not found with id: " + id));
	}

	public PublisherResponse createPublisher(PublisherRequest publisherRequest) {
		Publisher publisher = new Publisher();
		publisher.setName(publisherRequest.name());

		Publisher saved = publisherRepository.save(publisher);
		return toDto(saved);
	}

	public PublisherResponse updatePublisher(UUID id, PublisherRequest publisherRequest) {

		Publisher existing = publisherRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Publisher not found with id: " + id));

		existing.setName(publisherRequest.name());

		Publisher saved = publisherRepository.save(existing);
		return toDto(saved);
	}

	public void deletePublisher(UUID id) {
		if(!publisherRepository.existsById(id)){
			throw new ResourceNotFoundException("Publisher not found with id: " + id);
		}
		publisherRepository.deleteById(id);
	}

	private static PublisherResponse toDto(Publisher entity) {
		return new PublisherResponse(entity.getId(), entity.getName());
	}

}
