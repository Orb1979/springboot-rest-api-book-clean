package org.example.application.book;

import org.example.application.dto.BookResponse;

import java.util.List;

public interface ListBooksUseCase {

	List<BookResponse> getAllBooks();
}
