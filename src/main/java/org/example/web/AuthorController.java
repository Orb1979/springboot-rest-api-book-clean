package org.example.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.application.author.CreateAuthorUseCase;
import org.example.application.author.DeleteAuthorUseCase;
import org.example.application.author.GetAuthorUseCase;
import org.example.application.author.ListAuthorsUseCase;
import org.example.application.author.UpdateAuthorUseCase;
import org.example.application.dto.AuthorRequest;
import org.example.application.dto.AuthorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/authors")
@RequiredArgsConstructor
public class AuthorController {
	private final ListAuthorsUseCase listAuthors;
	private final GetAuthorUseCase getAuthor;
	private final CreateAuthorUseCase createAuthor;
	private final UpdateAuthorUseCase updateAuthor;
	private final DeleteAuthorUseCase deleteAuthor;

	@GetMapping
	public List<AuthorResponse> getAllAuthors() {
		return listAuthors.getAllAuthors();
	}

	@GetMapping("/{id}")
	public AuthorResponse getAuthorById(@PathVariable UUID id) {
		return getAuthor.getAuthorById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AuthorResponse createAuthor(@Valid @RequestBody AuthorRequest authorRequest) {
		return createAuthor.createAuthor(authorRequest);
	}

	@PutMapping("/{id}")
	public AuthorResponse updateAuthor(
			@PathVariable UUID id, @Valid @RequestBody AuthorRequest authorRequest) {
		return updateAuthor.updateAuthor(id, authorRequest);
	}

	@DeleteMapping("{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deleteAuthor(@PathVariable UUID id) {
		deleteAuthor.deleteAuthor(id);
	}
}
