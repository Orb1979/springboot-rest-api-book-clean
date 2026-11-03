package org.example.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.author.AuthorUseCase;
import org.example.application.author.AuthorRequest;
import org.example.application.author.AuthorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/authors")
@RequiredArgsConstructor
public class AuthorController {
	private final AuthorUseCase authorUseCase;

	@GetMapping
	public List<AuthorResponse> getAllAuthors() {
		return authorUseCase.getAllAuthors();
	}

	@GetMapping("/{id}")
	public AuthorResponse getAuthorById(@PathVariable UUID id) {
		return authorUseCase.getAuthorById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AuthorResponse createAuthor(@Valid @RequestBody AuthorRequest authorRequest) {
		return authorUseCase.createAuthor(authorRequest);
	}

	@PutMapping("/{id}")
	public AuthorResponse updateAuthor(
			@PathVariable UUID id, @Valid @RequestBody AuthorRequest authorRequest) {
		return authorUseCase.updateAuthor(id, authorRequest);
	}

	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteAuthor(@PathVariable UUID id) {
		authorUseCase.deleteAuthor(id);
	}
}
