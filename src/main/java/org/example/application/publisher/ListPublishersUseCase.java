package org.example.application.publisher;

import org.example.application.dto.PublisherResponse;

import java.util.List;

public interface ListPublishersUseCase {

	List<PublisherResponse> getAllPublishers();
}
