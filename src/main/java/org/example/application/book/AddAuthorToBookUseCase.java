package org.example.application.book;

import org.example.application.dto.BookResponse;

import java.util.UUID;

public interface AddAuthorToBookUseCase {

	BookResponse addAuthorToBook(UUID bookId, UUID authorId);
}
