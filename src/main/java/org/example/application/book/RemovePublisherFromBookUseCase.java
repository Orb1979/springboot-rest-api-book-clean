package org.example.application.book;

import org.example.application.dto.BookResponse;

import java.util.UUID;

public interface RemovePublisherFromBookUseCase {

	BookResponse removePublisherFromBook(UUID bookId);
}
