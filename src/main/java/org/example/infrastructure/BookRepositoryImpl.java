package org.example.infrastructure;

import org.example.domain.Book;
import org.example.domain.BookRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the domain {@link BookRepository} port
 * by delegating to Spring Data JPA.
 */
@Repository
public class BookRepositoryImpl implements BookRepository {

	private final SpringDataBookRepository delegate;

	public BookRepositoryImpl(SpringDataBookRepository delegate) {
		this.delegate = delegate;
	}

	@Override
	public List<Book> findAllWithDetailsBy() {
		return delegate.findAllWithDetailsBy();
	}

	@Override
	public Optional<Book> findWithDetailsById(UUID id) {
		return delegate.findWithDetailsById(id);
	}

	@Override
	public Optional<Book> findById(UUID id) {
		return delegate.findById(id);
	}

	@Override
	public boolean existsById(UUID id) {
		return delegate.existsById(id);
	}

	@Override
	public Book save(Book book) {
		return delegate.save(book);
	}

	@Override
	public void deleteById(UUID id) {
		delegate.deleteById(id);
	}
}
