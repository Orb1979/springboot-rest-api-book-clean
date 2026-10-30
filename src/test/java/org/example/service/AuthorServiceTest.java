package org.example.service;

import org.example.dto.AuthorRequest;
import org.example.dto.AuthorResponse;
import org.example.entity.Author;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.AuthorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

	@Mock	private AuthorRepository authorRepository;

	@InjectMocks private AuthorService authorService;


	@Test
	void getAllAuthorsReturnsAuthors() {
		Author author1 = new Author();
		author1.setId(UUID.randomUUID());
		author1.setFirstName("John");
		author1.setLastName("Doe");
		author1.setBirthDate(LocalDate.of(1990, 1, 1));

		Author author2 = new Author();
		author2.setId(UUID.randomUUID());
		author2.setFirstName("Jane");
		author2.setLastName("Doe");
		author2.setBirthDate(LocalDate.of(1992, 2, 2));

		when(authorRepository.findAll()).thenReturn(List.of(author1, author2));

		List<AuthorResponse> result = authorService.getAllAuthors();
		assertEquals(2, result.size());
		assertEquals(author1.getId(), result.getFirst().id());
		assertEquals(author1.getFirstName(), result.getFirst().firstName());
		assertEquals(author1.getLastName(), result.getFirst().lastName());
		assertEquals(author1.getBirthDate(), result.getFirst().birthDate());
		assertEquals(author2.getId(), result.get(1).id());
		assertEquals(author2.getFirstName(), result.get(1).firstName());
	}

	@Test
	void getAllAuthorsReturnsEmptyList() {
		when(authorRepository.findAll()).thenReturn(List.of());

		List<AuthorResponse> result = authorService.getAllAuthors();

		assertEquals(0, result.size());
	}

	@Test
	void getAuthorByIdReturnsAuthor() {
		UUID id = UUID.randomUUID();
		Author author = new Author();
		author.setId(id);
		author.setFirstName("John");
		author.setLastName("Doe");
		author.setBirthDate(LocalDate.of(1990, 1, 1));

		when(authorRepository.findById(id)).thenReturn(Optional.of(author));

		AuthorResponse result = authorService.getAuthorById(id);

		assertEquals(id, result.id());
		assertEquals("John", result.firstName());
		assertEquals("Doe", result.lastName());
		assertEquals(LocalDate.of(1990, 1, 1), result.birthDate());
	}

	@Test
	void getAuthorByIdReturnsNotFound() {
		UUID id = UUID.randomUUID();
		when(authorRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> authorService.getAuthorById(id));
	}

	@Test
	void createAuthorReturnsCreatedAuthor() {
		AuthorRequest requestDto = new AuthorRequest("John", "Doe", LocalDate.of(1990, 1, 1));
		Author author = new Author();
		author.setId(UUID.randomUUID());
		author.setFirstName("John");
		author.setLastName("Doe");
		author.setBirthDate(LocalDate.of(1990, 1, 1));

		when(authorRepository.save(any(Author.class))).thenReturn(author);

		AuthorResponse result = authorService.createAuthor(requestDto);

		assertEquals(author.getId(), result.id());
		assertEquals("John", result.firstName());
		assertEquals("Doe", result.lastName());
		assertEquals(LocalDate.of(1990, 1, 1), result.birthDate());

		verify(authorRepository).save(any(Author.class));
	}

	@Test
	void updateAuthorReturnsUpdatedAuthor() {
		UUID id = UUID.randomUUID();
		Author existingAuthor = new Author();
		existingAuthor.setId(id);
		existingAuthor.setFirstName("Old");
		existingAuthor.setLastName("Name");
		existingAuthor.setBirthDate(LocalDate.of(1970, 1, 1));

		AuthorRequest requestDto = new AuthorRequest("New", "Name", LocalDate.of(1990, 1, 1));

		when(authorRepository.findById(id)).thenReturn(Optional.of(existingAuthor));
		when(authorRepository.save(existingAuthor)).thenReturn(existingAuthor);

		AuthorResponse result = authorService.updateAuthor(id, requestDto);

		assertEquals(id, result.id());
		assertEquals("New", result.firstName());
		assertEquals("Name", result.lastName());
		assertEquals(LocalDate.of(1990, 1, 1), result.birthDate());
		verify(authorRepository).save(existingAuthor);
	}

	@Test
	void updateAuthorReturnsNotFound() {
		UUID id = UUID.randomUUID();
		when(authorRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(
				ResourceNotFoundException.class,
				() -> authorService.updateAuthor(id, new AuthorRequest("a", "b", null)));
		verify(authorRepository, never()).save(any());
	}

	@Test
	void deleteAuthorReturnsOk() {
		UUID id = UUID.randomUUID();
		when(authorRepository.existsById(id)).thenReturn(true);

		authorService.deleteAuthor(id);

		verify(authorRepository).deleteById(id);
	}

	@Test
	void deleteAuthorReturnsNotFound() {
		UUID id = UUID.randomUUID();
		when(authorRepository.existsById(id)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> authorService.deleteAuthor(id));
		verify(authorRepository, never()).deleteById(any());
	}
}
