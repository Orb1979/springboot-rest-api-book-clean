package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record BookRequest(
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 500) String subTitle,
    @Size(max = 5000) String description,
    @Positive int pages,
    @Size(max = 20) String isbn,
    UUID publisherId,
    List<UUID> authorIds) {}
