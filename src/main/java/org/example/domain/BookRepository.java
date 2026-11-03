package org.example.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for book persistence. The application layer depends only on this
 * interface; the infrastructure layer provides the implementation.
 */
public interface BookRepository {

	List<Book> findAllWithDetailsBy();

	Optional<Book> findWithDetailsById(UUID id);

	Optional<Book> findById(UUID id);

	boolean existsById(UUID id);

	Book save(Book book);

	void deleteById(UUID id);
}
