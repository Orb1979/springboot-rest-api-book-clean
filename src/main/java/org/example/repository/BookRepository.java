package org.example.repository;

import org.example.entity.AuthorBook_;
import org.example.entity.Book;
import org.example.entity.Book_;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

	@EntityGraph(attributePaths = {
			Book_.AUTHOR_BOOKS,
			Book_.AUTHOR_BOOKS + "." + AuthorBook_.AUTHOR,
			Book_.PUBLISHER
	})
	Optional<Book> findWithDetailsById(UUID id);

	@EntityGraph(attributePaths = {
			Book_.AUTHOR_BOOKS,
			Book_.AUTHOR_BOOKS + "." + AuthorBook_.AUTHOR,
			Book_.PUBLISHER
	})
	List<Book> findAllWithDetailsBy();

	/*
		Example with Join Fetch:

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

	Notes about EntityGraph:
	- The code is the same, but you don't need to write any jpql
	- Notice the derived method name with 'Detail', if you use other name you would need to add @Query again
	- normally the attributes paths ok book are written as a String like (attributePaths = {"authorBooks"}
		- 1. Add to Gradle/Maven a Hibernate a annotationProcessor
		- 2. IntelliJ:  Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation
		- 3. now we can specify it like 	{ Book_.AUTHOR_BOOKS }
		this is more type safe, if we e.g. rename Book.authorBooks, we would get a compile error.

		Annotation values must be compile-time constants, The only thing it can accept is a String.
		A compile-time constant is a value the compiler can work out while compiling, without executing any of your code.
		The generator solves this by reading your entities at compile time and writing a companion class (Book_) containing
		a String constant for every attribute. Those constants are regenerated on every build from the real fields.

	@EntityGraph(attributePaths = {"authorBooks", "authorBooks.author", "publisher"})
	@Query("SELECT b FROM Book b WHERE b.id = :id")
	Optional<Book> findByIdHydrated(UUID id);

	@EntityGraph(attributePaths = {"authorBooks", "authorBooks.author", "publisher"})
	@Query("SELECT b FROM Book b")
	List<Book> findAllHydrated();
	*/

}


