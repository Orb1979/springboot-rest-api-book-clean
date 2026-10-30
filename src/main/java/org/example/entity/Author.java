package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Author {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String firstName;

	private String lastName;

	private LocalDate birthDate;

	@OneToMany(mappedBy = "author",  cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<AuthorBook> authorBooks = new ArrayList<>();


}
