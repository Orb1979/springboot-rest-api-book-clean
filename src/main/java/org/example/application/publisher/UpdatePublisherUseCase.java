package org.example.application.publisher;

import org.example.application.dto.PublisherRequest;
import org.example.application.dto.PublisherResponse;

import java.util.UUID;

public interface UpdatePublisherUseCase {

	PublisherResponse updatePublisher(UUID id, PublisherRequest request);
}
