package org.example.application.book;

import org.example.application.dto.BookRequest;
import org.example.application.dto.BookResponse;

import java.util.List;
import java.util.UUID;

/**
 * All book operations offered by the application layer.
 * Implemented by {@link BookService}; consumed by the presentation layer.
 */
public interface BookUseCase {

	List<BookResponse> getAllBooks();

	BookResponse getBookById(UUID id);

	BookResponse createBook(BookRequest request);

	BookResponse updateBook(UUID id, BookRequest request);

	void deleteBook(UUID id);

	BookResponse addAuthorToBook(UUID bookId, UUID authorId);

	BookResponse removeAuthorFromBook(UUID bookId, UUID authorId);

	BookResponse replaceAuthors(UUID bookId, List<UUID> authorIds);

	BookResponse removePublisherFromBook(UUID bookId);
}
