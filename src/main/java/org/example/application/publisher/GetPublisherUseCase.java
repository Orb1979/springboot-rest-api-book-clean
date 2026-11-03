package org.example.application.publisher;

import org.example.application.dto.PublisherResponse;

import java.util.UUID;

public interface GetPublisherUseCase {

	PublisherResponse getPublisherById(UUID id);
}
