package org.example.infrastructure;

import org.example.domain.Publisher;
import org.example.domain.PublisherRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the domain {@link PublisherRepository} port
 * by delegating to Spring Data JPA.
 */
@Repository
public class PublisherRepositoryImpl implements PublisherRepository {

	private final SpringDataPublisherRepository delegate;

	public PublisherRepositoryImpl(SpringDataPublisherRepository delegate) {
		this.delegate = delegate;
	}

	@Override
	public List<Publisher> findAll() {
		return delegate.findAll();
	}

	@Override
	public Optional<Publisher> findById(UUID id) {
		return delegate.findById(id);
	}

	@Override
	public boolean existsById(UUID id) {
		return delegate.existsById(id);
	}

	@Override
	public Publisher save(Publisher publisher) {
		return delegate.save(publisher);
	}

	@Override
	public void deleteById(UUID id) {
		delegate.deleteById(id);
	}
}
