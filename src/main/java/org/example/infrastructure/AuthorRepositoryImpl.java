package org.example.infrastructure;

import org.example.domain.Author;
import org.example.domain.AuthorRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the domain {@link AuthorRepository} port
 * by delegating to Spring Data JPA.
 */
@Repository
public class AuthorRepositoryImpl implements AuthorRepository {

	private final SpringDataAuthorRepository delegate;

	public AuthorRepositoryImpl(SpringDataAuthorRepository delegate) {
		this.delegate = delegate;
	}

	@Override
	public List<Author> findAll() {
		return delegate.findAll();
	}

	@Override
	public List<Author> findAllById(Iterable<UUID> ids) {
		return delegate.findAllById(ids);
	}

	@Override
	public Optional<Author> findById(UUID id) {
		return delegate.findById(id);
	}

	@Override
	public boolean existsById(UUID id) {
		return delegate.existsById(id);
	}

	@Override
	public Author save(Author author) {
		return delegate.save(author);
	}

	@Override
	public void deleteById(UUID id) {
		delegate.deleteById(id);
	}
}
