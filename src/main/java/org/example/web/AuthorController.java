package org.example.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.AuthorRequest;
import org.example.dto.AuthorResponse;
import org.example.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/authors")
@RequiredArgsConstructor
public class AuthorController {
	private final AuthorService authorService;

	@GetMapping
	public List<AuthorResponse> getAllAuthors() {
		return authorService.getAllAuthors();
	}

	@GetMapping("/{id}")
	public AuthorResponse getAuthorById(@PathVariable UUID id) {
		return authorService.getAuthorById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AuthorResponse createAuthor(@Valid @RequestBody AuthorRequest authorRequest) {
		return authorService.createAuthor(authorRequest);
	}

	@PutMapping("/{id}")
	public AuthorResponse updateAuthor(
			@PathVariable UUID id, @Valid @RequestBody AuthorRequest authorRequest) {
		return authorService.updateAuthor(id, authorRequest);
	}

	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteAuthor(@PathVariable UUID id) {
		authorService.deleteAuthor(id);
	}
}
