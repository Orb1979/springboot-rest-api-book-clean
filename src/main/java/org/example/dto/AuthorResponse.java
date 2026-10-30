package org.example.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AuthorResponse(UUID id, String firstName, String lastName, LocalDate birthDate) {}
