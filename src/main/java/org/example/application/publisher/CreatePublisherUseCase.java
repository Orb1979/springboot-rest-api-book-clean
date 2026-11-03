package org.example.application.publisher;

import org.example.application.dto.PublisherRequest;
import org.example.application.dto.PublisherResponse;

public interface CreatePublisherUseCase {

	PublisherResponse createPublisher(PublisherRequest request);
}
