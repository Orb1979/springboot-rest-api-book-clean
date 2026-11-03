package org.example.application.book;

import org.example.application.dto.BookResponse;

import java.util.List;
import java.util.UUID;

public interface ReplaceAuthorsUseCase {

	BookResponse replaceAuthors(UUID bookId, List<UUID> authorIds);
}
