package org.example.application.book;

import org.example.application.dto.BookRequest;
import org.example.application.dto.BookResponse;

import java.util.UUID;

public interface UpdateBookUseCase {

	BookResponse updateBook(UUID id, BookRequest request);
}
