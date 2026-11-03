package org.example.application.publisher;


import java.util.List;
import java.util.UUID;

/**
 * All publisher operations offered by the application layer.
 * Implemented by {@link PublisherService}; consumed by the presentation layer.
 */
public interface PublisherUseCase {

	List<PublisherResponse> getAllPublishers();

	PublisherResponse getPublisherById(UUID id);

	PublisherResponse createPublisher(PublisherRequest request);

	PublisherResponse updatePublisher(UUID id, PublisherRequest request);

	void deletePublisher(UUID id);
}
