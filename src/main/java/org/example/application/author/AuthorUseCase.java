package org.example.application.author;


import java.util.List;
import java.util.UUID;

/**
 * All author operations offered by the application layer.
 * Implemented by {@link AuthorService}; consumed by the presentation layer.
 */
public interface AuthorUseCase {

	List<AuthorResponse> getAllAuthors();

	AuthorResponse getAuthorById(UUID id);

	AuthorResponse createAuthor(AuthorRequest request);

	AuthorResponse updateAuthor(UUID id, AuthorRequest request);

	void deleteAuthor(UUID id);
}
