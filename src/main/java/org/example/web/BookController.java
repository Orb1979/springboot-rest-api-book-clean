package org.example.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.book.AddAuthorToBookUseCase;
import org.example.application.book.CreateBookUseCase;
import org.example.application.book.DeleteBookUseCase;
import org.example.application.book.GetBookUseCase;
import org.example.application.book.ListBooksUseCase;
import org.example.application.book.RemoveAuthorFromBookUseCase;
import org.example.application.book.RemovePublisherFromBookUseCase;
import org.example.application.book.ReplaceAuthorsUseCase;
import org.example.application.book.UpdateBookUseCase;
import org.example.application.dto.BookRequest;
import org.example.application.dto.BookResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {
	private final ListBooksUseCase listBooks;
	private final GetBookUseCase getBook;
	private final CreateBookUseCase createBook;
	private final UpdateBookUseCase updateBook;
	private final DeleteBookUseCase deleteBook;
	private final AddAuthorToBookUseCase addAuthorToBook;
	private final RemoveAuthorFromBookUseCase removeAuthorFromBook;
	private final ReplaceAuthorsUseCase replaceAuthors;
	private final RemovePublisherFromBookUseCase removePublisherFromBook;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<BookResponse> getAllBooks() {
		return listBooks.getAllBooks();
	}

	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse getBookById(@PathVariable UUID id) {
		return getBook.getBookById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BookResponse createBook(@Valid @RequestBody BookRequest bookRequestDto) {
		return createBook.createBook(bookRequestDto);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse updateBook(@PathVariable UUID id, @Valid @RequestBody BookRequest bookRequestDto) {
		return updateBook.updateBook(id, bookRequestDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteBook(@PathVariable UUID id) {
		deleteBook.deleteBook(id);
	}

	// --- Granular Sub-Resource Endpoints ---

	@PutMapping("/{id}/authors/{authorId}")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse addAuthorToBook(@PathVariable UUID id, @PathVariable UUID authorId) {
		return addAuthorToBook.addAuthorToBook(id, authorId);
	}

	// Convenience controller, could be replaced with addAuthorToBook and removeAuthorFromBook
	@PutMapping("/{id}/authors")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse replaceAuthors(@PathVariable UUID id, @RequestBody List<UUID> authorIds) {
		return replaceAuthors.replaceAuthors(id, authorIds);
	}

	@DeleteMapping("/{id}/authors/{authorId}")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse removeAuthorFromBook(@PathVariable UUID id, @PathVariable UUID authorId) {
		return removeAuthorFromBook.removeAuthorFromBook(id, authorId);
	}

	@DeleteMapping("/{id}/publisher")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse removePublisherFromBook(@PathVariable UUID id) {
		return removePublisherFromBook.removePublisherFromBook(id);
	}
}
