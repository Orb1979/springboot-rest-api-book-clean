package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.AuthorRequest;
import org.example.dto.AuthorResponse;
import org.example.entity.Author;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.AuthorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthorService {
	private final AuthorRepository authorRepository;

	public List<AuthorResponse> getAllAuthors() {
		return authorRepository.findAll().stream().map(AuthorService::toDto).toList();
	}

	public AuthorResponse getAuthorById(UUID id) {
		return authorRepository.findById(id).map(AuthorService::toDto).orElseThrow(
				() -> new ResourceNotFoundException("Author not found with id: " + id));
	}

	public AuthorResponse createAuthor(AuthorRequest authorRequest) {
		Author author = new Author();
		author.setFirstName(authorRequest.firstName());
		author.setLastName(authorRequest.lastName());
		author.setBirthDate(authorRequest.birthDate());

		Author saved = authorRepository.save(author);
		return toDto(saved);
	}

	public AuthorResponse updateAuthor(UUID id, AuthorRequest authorRequest) {
		Author existing = authorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));

		existing.setFirstName(authorRequest.firstName());
		existing.setLastName(authorRequest.lastName());
		existing.setBirthDate(authorRequest.birthDate());

		Author saved = authorRepository.save(existing);
		return toDto(saved);
	}

	public void deleteAuthor(UUID id) {
		if (!authorRepository.existsById(id)) {
			throw new ResourceNotFoundException("Author not found with id: " + id);
		}
		authorRepository.deleteById(id);
	}

	private static AuthorResponse toDto(Author entity) {
		return new AuthorResponse(entity.getId(), entity.getFirstName(), entity.getLastName(), entity.getBirthDate());
	}
}
