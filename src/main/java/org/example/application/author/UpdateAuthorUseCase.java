package org.example.application.author;

import org.example.application.dto.AuthorRequest;
import org.example.application.dto.AuthorResponse;

import java.util.UUID;

public interface UpdateAuthorUseCase {

	AuthorResponse updateAuthor(UUID id, AuthorRequest request);
}
