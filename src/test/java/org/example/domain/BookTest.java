package org.example.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/*
Pure unit tests of the Book aggregate: no mocks, no Spring, no database.
Book owns every rule about its author links (addAuthor, removeAuthor,
replaceAuthors), so these tests are the executable specification for them.
*/
class BookTest {

	private Author author(UUID id) {
		Author author = new Author();
		author.setId(id);
		return author;
	}

	private Book book() {
		Book book = new Book();
		book.setId(UUID.randomUUID());
		return book;
	}

	@Test
	void addAuthorLinksAuthorToBook() {
		Book book = book();
		Author author = author(UUID.randomUUID());

		boolean added = book.addAuthor(author);

		assertThat(added).isTrue();
		assertThat(book.getAuthors()).containsExactly(author);
	}

	@Test
	void addAuthorIsIdempotent() {
		Book book = book();
		Author author = author(UUID.randomUUID());
		book.addAuthor(author);

		boolean addedAgain = book.addAuthor(author);

		assertThat(addedAgain).isFalse();
		assertThat(book.getAuthors()).hasSize(1);
	}

	@Test
	void removeAuthorRemovesLink() {
		Book book = book();
		Author author = author(UUID.randomUUID());
		book.addAuthor(author);

		boolean removed = book.removeAuthor(author.getId());

		assertThat(removed).isTrue();
		assertThat(book.getAuthors()).isEmpty();
	}

	@Test
	void removeAuthorReturnsFalseWhenNotLinked() {
		Book book = book();

		boolean removed = book.removeAuthor(UUID.randomUUID());

		assertThat(removed).isFalse();
	}

	@Test
	void replaceAuthorsRemovesLinksToAbsentAuthors() {
		Book book = book();
		Author oldAuthor = author(UUID.randomUUID());
		Author keptAuthor = author(UUID.randomUUID());
		Author newAuthor = author(UUID.randomUUID());
		book.addAuthor(oldAuthor);
		book.addAuthor(keptAuthor);

		book.replaceAuthors(List.of(keptAuthor, newAuthor));

		assertThat(book.getAuthors())
				.extracting(Author::getId)
				.containsExactly(keptAuthor.getId(), newAuthor.getId());
	}

	@Test
	void replaceAuthorsWithEmptyListClearsAllLinks() {
		Book book = book();
		book.addAuthor(author(UUID.randomUUID()));
		book.addAuthor(author(UUID.randomUUID()));

		book.replaceAuthors(List.of());

		assertThat(book.getAuthors()).isEmpty();
	}

	@Test
	void replaceAuthorsIsNoOpWhenSameAuthors() {
		Book book = book();
		Author first = author(UUID.randomUUID());
		Author second = author(UUID.randomUUID());
		book.addAuthor(first);
		book.addAuthor(second);

		book.replaceAuthors(List.of(second, first));

		assertThat(book.getAuthors())
				.extracting(Author::getId)
				.containsExactlyInAnyOrder(first.getId(), second.getId());
	}

	@Test
	void replaceAuthorsToleratesDuplicatesInRequest() {
		Book book = book();
		Author first = author(UUID.randomUUID());
		Author second = author(UUID.randomUUID());

		book.replaceAuthors(List.of(first, first, second));

		assertThat(book.getAuthors())
				.extracting(Author::getId)
				.containsExactlyInAnyOrder(first.getId(), second.getId());
	}

	@Test
	void getAuthorsIsUnmodifiable() {
		Book book = book();
		book.addAuthor(author(UUID.randomUUID()));

		assertThatThrownBy(() -> book.getAuthors().add(author(UUID.randomUUID())))
				.isInstanceOf(UnsupportedOperationException.class);
	}
}
