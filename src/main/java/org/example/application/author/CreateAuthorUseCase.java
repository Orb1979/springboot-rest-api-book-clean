package org.example.application.author;

import org.example.application.dto.AuthorRequest;
import org.example.application.dto.AuthorResponse;

public interface CreateAuthorUseCase {

	AuthorResponse createAuthor(AuthorRequest request);
}
