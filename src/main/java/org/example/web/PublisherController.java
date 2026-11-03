package org.example.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.dto.PublisherRequest;
import org.example.application.dto.PublisherResponse;
import org.example.application.publisher.PublisherUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/publishers")
@RequiredArgsConstructor
public class PublisherController {
	private final PublisherUseCase publisherUseCase;

	@GetMapping
	public List<PublisherResponse> getAllPublishers() {
		return publisherUseCase.getAllPublishers();
	}

	@GetMapping("/{id}")
	public PublisherResponse getPublisherById(@PathVariable UUID id) {
		return publisherUseCase.getPublisherById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PublisherResponse createPublisher(@Valid @RequestBody PublisherRequest publisherRequest) {
		return publisherUseCase.createPublisher(publisherRequest);
	}

	@PutMapping("/{id}")
	public PublisherResponse updatePublisher(
			@PathVariable UUID id, @Valid @RequestBody PublisherRequest publisherRequest) {
		return publisherUseCase.updatePublisher(id, publisherRequest);
	}

	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePublisher(@PathVariable UUID id) {
		publisherUseCase.deletePublisher(id);
	}

}
