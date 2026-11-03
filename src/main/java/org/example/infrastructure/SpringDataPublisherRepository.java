package org.example.infrastructure;

import org.example.domain.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataPublisherRepository extends JpaRepository<Publisher, UUID> {
}
