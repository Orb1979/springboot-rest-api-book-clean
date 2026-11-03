package org.example.infrastructure;

import org.example.domain.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataAuthorRepository extends JpaRepository<Author, UUID> {
}
