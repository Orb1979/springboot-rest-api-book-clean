package org.example.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.BookRequest;
import org.example.dto.BookResponse;
import org.example.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {
	private final BookService bookService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public List<BookResponse> getAllBooks() {
		return bookService.getAllBooks();
	}

	@GetMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse getBookById(@PathVariable UUID id) {
		return bookService.getBookById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BookResponse createBook(@Valid @RequestBody BookRequest bookRequestDto) {
		return bookService.createBook(bookRequestDto);
	}

	@PutMapping("/{id}")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse updateBook(@PathVariable UUID id, @Valid @RequestBody BookRequest bookRequestDto) {
		return bookService.updateBook(id, bookRequestDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteBook(@PathVariable UUID id) {
		bookService.deleteBook(id);
	}

	// --- Granular Sub-Resource Endpoints ---

	@PutMapping("/{id}/authors/{authorId}")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse addAuthorToBook(@PathVariable UUID id, @PathVariable UUID authorId) {
		return bookService.addAuthorToBook(id, authorId);
	}

	// Convenience controller, could be replaced with addAuthorToBook and removeAuthorFromBook
	@PutMapping("/{id}/authors")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse replaceAuthors(@PathVariable UUID id, @RequestBody List<UUID> authorIds) {
		return bookService.replaceAuthors(id, authorIds);
	}

	@DeleteMapping("/{id}/authors/{authorId}")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse removeAuthorFromBook(@PathVariable UUID id, @PathVariable UUID authorId) {
		return bookService.removeAuthorFromBook(id, authorId);
	}

	@DeleteMapping("/{id}/publisher")
	@ResponseStatus(HttpStatus.OK)
	public BookResponse removePublisherFromBook(@PathVariable UUID id) {
		return bookService.removePublisherFromBook(id);
	}
}
