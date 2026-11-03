package org.example.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.publisher.CreatePublisherUseCase;
import org.example.application.publisher.DeletePublisherUseCase;
import org.example.application.publisher.GetPublisherUseCase;
import org.example.application.publisher.ListPublishersUseCase;
import org.example.application.publisher.UpdatePublisherUseCase;
import org.example.application.dto.PublisherRequest;
import org.example.application.dto.PublisherResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/publishers")
@RequiredArgsConstructor
public class PublisherController {
	private final ListPublishersUseCase listPublishers;
	private final GetPublisherUseCase getPublisher;
	private final CreatePublisherUseCase createPublisher;
	private final UpdatePublisherUseCase updatePublisher;
	private final DeletePublisherUseCase deletePublisher;

	@GetMapping
	public List<PublisherResponse> getAllPublishers() {
		return listPublishers.getAllPublishers();
	}

	@GetMapping("/{id}")
	public PublisherResponse getPublisherById(@PathVariable UUID id) {
		return getPublisher.getPublisherById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PublisherResponse createPublisher(@Valid @RequestBody PublisherRequest publisherRequest) {
		return createPublisher.createPublisher(publisherRequest);
	}

	@PutMapping("/{id}")
	public PublisherResponse updatePublisher(
			@PathVariable UUID id, @Valid @RequestBody PublisherRequest publisherRequest) {
		return updatePublisher.updatePublisher(id, publisherRequest);
	}

	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePublisher(@PathVariable UUID id) {
		deletePublisher.deletePublisher(id);
	}

}
