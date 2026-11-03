package org.example.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for author persistence. The application layer depends only on this
 * interface; the infrastructure layer provides the implementation.
 */
public interface AuthorRepository {

	List<Author> findAll();

	List<Author> findAllById(Iterable<UUID> ids);

	Optional<Author> findById(UUID id);

	boolean existsById(UUID id);

	Author save(Author author);

	void deleteById(UUID id);
}
