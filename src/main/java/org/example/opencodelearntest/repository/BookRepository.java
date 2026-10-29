package org.example.opencodelearntest.repository;

import org.example.opencodelearntest.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

	@Query(
			"""
		SELECT b
		FROM Book b
		LEFT JOIN FETCH b.authorBooks ab
		LEFT JOIN FETCH ab.author
		LEFT JOIN FETCH b.publisher
		WHERE b.id = :id
""")
	Optional<Book> findByIdHydrated(UUID id);

	@Query(
			"""
		SELECT b
		FROM Book b
		LEFT JOIN FETCH b.authorBooks ab
		LEFT JOIN FETCH ab.author
		LEFT JOIN FETCH b.publisher
""")
	List<Book> findAllHydrated();
}


