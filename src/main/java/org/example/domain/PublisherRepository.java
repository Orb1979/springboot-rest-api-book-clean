package org.example.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain port for publisher persistence. The application layer depends only on this
 * interface; the infrastructure layer provides the implementation.
 */
public interface PublisherRepository {

	List<Publisher> findAll();

	Optional<Publisher> findById(UUID id);

	boolean existsById(UUID id);

	Publisher save(Publisher publisher);

	void deleteById(UUID id);
}
