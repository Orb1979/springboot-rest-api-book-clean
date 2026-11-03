package org.example.application.author;

import org.example.application.dto.AuthorResponse;

import java.util.UUID;

public interface GetAuthorUseCase {

	AuthorResponse getAuthorById(UUID id);
}
