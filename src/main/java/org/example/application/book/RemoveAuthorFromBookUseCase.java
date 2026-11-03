package org.example.application.book;

import org.example.application.dto.BookResponse;

import java.util.UUID;

public interface RemoveAuthorFromBookUseCase {

	BookResponse removeAuthorFromBook(UUID bookId, UUID authorId);
}
