package org.example.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.book.BookUseCase;
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
	private final BookUseCase bookUseCase;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<BookResponse> getAllBooks() {
		return bookUseCase.getAllBooks();
	}

	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse getBookById(@PathVariable UUID id) {
		return bookUseCase.getBookById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BookResponse createBook(@Valid @RequestBody BookRequest bookRequestDto) {
		return bookUseCase.createBook(bookRequestDto);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse updateBook(@PathVariable UUID id, @Valid @RequestBody BookRequest bookRequestDto) {
		return bookUseCase.updateBook(id, bookRequestDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteBook(@PathVariable UUID id) {
		bookUseCase.deleteBook(id);
	}

	// --- Granular Sub-Resource Endpoints ---

	@PutMapping("/{id}/authors/{authorId}")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse addAuthorToBook(@PathVariable UUID id, @PathVariable UUID authorId) {
		return bookUseCase.addAuthorToBook(id, authorId);
	}

	// Convenience controller, could be replaced with addAuthorToBook and removeAuthorFromBook
	@PutMapping("/{id}/authors")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse replaceAuthors(@PathVariable UUID id, @RequestBody List<UUID> authorIds) {
		return bookUseCase.replaceAuthors(id, authorIds);
	}

	@DeleteMapping("/{id}/authors/{authorId}")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse removeAuthorFromBook(@PathVariable UUID id, @PathVariable UUID authorId) {
		return bookUseCase.removeAuthorFromBook(id, authorId);
	}

	@DeleteMapping("/{id}/publisher")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse removePublisherFromBook(@PathVariable UUID id) {
		return bookUseCase.removePublisherFromBook(id);
	}
}
