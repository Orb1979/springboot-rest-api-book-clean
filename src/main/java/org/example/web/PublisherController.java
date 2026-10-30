package org.example.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.PublisherRequest;
import org.example.dto.PublisherResponse;
import org.example.service.PublisherService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/publishers")
@RequiredArgsConstructor
public class PublisherController {
	private final PublisherService publisherService;

	@GetMapping
	public List<PublisherResponse> getAllPublishers() {
		return publisherService.getAllPublishers();
	}

	@GetMapping("/{id}")
	public PublisherResponse getPublisherById(@PathVariable UUID id) {
		return publisherService.getPublisherById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PublisherResponse createPublisher(@Valid @RequestBody PublisherRequest publisherRequest) {
		return publisherService.createPublisher(publisherRequest);
	}

	@PutMapping("/{id}")
	public PublisherResponse updatePublisher(
			@PathVariable UUID id, @Valid @RequestBody PublisherRequest publisherRequest) {
		return publisherService.updatePublisher(id, publisherRequest);
	}

	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePublisher(@PathVariable UUID id) {
		publisherService.deletePublisher(id);
	}

}
