package org.example.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String title;

	private String subTitle;

	private String description;

	private Integer pages;

	private String isbn;

	// Author links are part of this aggregate: they are created and removed
	// only through the behavior methods below, never from the outside.
	@Getter(AccessLevel.NONE)
	@OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<AuthorBook> authorBooks = new ArrayList<>();

	@ManyToOne()
	@JoinColumn(name = "publisher_id")
	private Publisher publisher;

	/** Read-only view of the authors linked to this book. */
	public List<Author> getAuthors() {
		return authorBooks.stream().map(AuthorBook::getAuthor).toList();
	}

	/** Links the author to this book. Idempotent: returns false if already linked. */
	public boolean addAuthor(Author author) {
		if (hasAuthor(author.getId())) {
			return false;
		}
		authorBooks.add(new AuthorBook(author, this));
		return true;
	}

	/** Removes the author link. Returns false if the author was not linked. */
	public boolean removeAuthor(UUID authorId) {
		return authorBooks.removeIf(ab -> ab.getAuthor().getId().equals(authorId));
	}

	/**
	 * Replaces the author links with exactly the given authors: removes links to
	 * authors that are no longer desired, then adds any missing ones. Duplicate
	 * entries in the list are tolerated.
	 */
	public void replaceAuthors(List<Author> desiredAuthors) {
		Set<UUID> desiredIds =
				desiredAuthors.stream().map(Author::getId).collect(Collectors.toSet());
		authorBooks.removeIf(ab -> !desiredIds.contains(ab.getAuthor().getId()));
		desiredAuthors.forEach(this::addAuthor);
	}

	private boolean hasAuthor(UUID authorId) {
		return authorBooks.stream().anyMatch(ab -> ab.getAuthor().getId().equals(authorId));
	}
}
