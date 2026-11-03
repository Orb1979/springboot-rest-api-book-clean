package org.example.application.book;

import org.example.application.dto.BookRequest;
import org.example.application.dto.BookResponse;

public interface CreateBookUseCase {

	BookResponse createBook(BookRequest request);
}
