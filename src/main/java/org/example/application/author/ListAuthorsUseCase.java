package org.example.application.author;

import org.example.application.dto.AuthorResponse;

import java.util.List;

public interface ListAuthorsUseCase {

	List<AuthorResponse> getAllAuthors();
}
