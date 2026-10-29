package org.example.opencodelearntest.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String title;

	private String subTitle;

	private String description;

	private Integer pages;

	private String isbn;

	@OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<AuthorBook> authorBooks = new ArrayList<>();

	@ManyToOne()
	@JoinColumn(name = "publisher_id")
	private Publisher publisher;
}
