package org.example.dto;

import java.util.List;
import java.util.UUID;

public record BookResponse(
    UUID id,
    String title,
    String subTitle,
    String description,
    int pages,
    String isbn,
    PublisherResponse publisher,
    List<AuthorResponse> authors) {}
