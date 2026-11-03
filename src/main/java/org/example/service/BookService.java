package org.example.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.AuthorResponse;
import org.example.dto.BookRequest;
import org.example.dto.BookResponse;
import org.example.dto.PublisherResponse;
import org.example.entity.Author;
import org.example.entity.AuthorBook;
import org.example.entity.Book;
import org.example.entity.Publisher;
import org.example.exception.BadRequestException;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.AuthorRepository;
import org.example.repository.BookRepository;
import org.example.repository.PublisherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {
	private final BookRepository bookRepository;
	private final PublisherRepository publisherRepository;
	private final AuthorRepository authorRepository;

	public List<BookResponse> getAllBooks() {
		return bookRepository.findAllWithDetailsBy().stream().map(BookService::toResponseDto).toList();
	}

	public BookResponse getBookById(UUID id) {
		Book book = bookRepository.findWithDetailsById(id).orElseThrow(
				() -> new ResourceNotFoundException("Book not found, id: " + id));
		return toResponseDto(book);
	}

	public BookResponse createBook(@Valid BookRequest bookRequest) {
		Book book = toEntity(bookRequest);
		Book saved = bookRepository.save(book);
		return toResponseDto(saved);
	}

	@Transactional
	public BookResponse updateBook(UUID id, BookRequest bookRequestDto) {
		Book existingBook =
				bookRepository
						.findWithDetailsById(id)
						.orElseThrow(() -> new ResourceNotFoundException("Book not found, id: " + id));
		existingBook.setTitle(bookRequestDto.title());
		existingBook.setSubTitle(bookRequestDto.subTitle());
		existingBook.setDescription(bookRequestDto.description());
		existingBook.setPages(bookRequestDto.pages());
		existingBook.setIsbn(bookRequestDto.isbn());
		if (bookRequestDto.publisherId() != null) {
			existingBook.setPublisher(resolvePublisher(bookRequestDto.publisherId()));
		}

		syncAuthors(existingBook, bookRequestDto.authorIds());

		Book saved = bookRepository.save(existingBook);
		return toResponseDto(saved);
	}

	public void deleteBook(UUID id) {
		if (!bookRepository.existsById(id)) {
			throw new ResourceNotFoundException("Book not found, id: " + id);
		}
		bookRepository.deleteById(id);
	}

	@Transactional
	public BookResponse removePublisherFromBook(UUID bookId) {
		Book book =
				bookRepository
						.findById(bookId)
						.orElseThrow(() -> new ResourceNotFoundException("Book not found"));

		book.setPublisher(null);
		bookRepository.save(book);
		return getBookById(bookId);
	}

	@Transactional
	public BookResponse addAuthorToBook(UUID bookId, UUID authorId) {
		Book book =
				bookRepository
						.findById(bookId)
						.orElseThrow(() -> new ResourceNotFoundException("Book not found"));

		Author author =
				authorRepository
						.findById(authorId)
						.orElseThrow(() -> new ResourceNotFoundException("Author not found"));

		boolean exists = book.getAuthorBooks().stream().anyMatch(ab -> ab.getAuthor().getId().equals(authorId));
		if (!exists) {
			book.getAuthorBooks().add(new AuthorBook(author, book));
			bookRepository.save(book);
		}
		return getBookById(bookId);
	}

	@Transactional
	public BookResponse removeAuthorFromBook(UUID bookId, UUID authorId) {
		Book book = bookRepository.findById(bookId)
						.orElseThrow(() -> new ResourceNotFoundException("Book not found"));

		book.getAuthorBooks().removeIf(ab -> ab.getAuthor().getId().equals(authorId));
		bookRepository.save(book);
		return getBookById(bookId);
	}

	@Transactional
	public BookResponse replaceAuthors(UUID bookId, List<UUID> authorIds) {
		if (authorIds == null) {
			throw new BadRequestException("authorIds cannot be null");
		}

		Book book = bookRepository.findWithDetailsById(bookId)
						.orElseThrow(() -> new ResourceNotFoundException("Book not found"));

		syncAuthors(book, authorIds);

		Book saved = bookRepository.save(book);
		return toResponseDto(saved);
	}

	private static BookResponse toResponseDto(Book book) {
		if (book == null) {
			return null;
		}
		return new BookResponse(
				book.getId(),
				book.getTitle(),
				book.getSubTitle(),
				book.getDescription(),
				book.getPages(),
				book.getIsbn(),
				book.getPublisher() != null
				? new PublisherResponse(
						book.getPublisher().getId(),
						book.getPublisher().getName()
				) : null,
				book.getAuthorBooks().stream()
						.map(AuthorBook::getAuthor)
						.map(author -> new AuthorResponse(
								author.getId(),
								author.getFirstName(),
								author.getLastName(),
								author.getBirthDate()
						)).toList()
		);
	}

	private Book toEntity(BookRequest dto) {
		if (dto == null) {
			return null;
		}

		Book book = new Book();
		book.setTitle(dto.title());
		book.setSubTitle(dto.subTitle());
		book.setDescription(dto.description());
		book.setPages(dto.pages());
		book.setIsbn(dto.isbn());
		if (dto.publisherId() != null) {
			book.setPublisher(resolvePublisher(dto.publisherId()));
		}
		if (dto.authorIds() != null) {
			resolveAuthors(dto.authorIds())
					.forEach(author -> book.getAuthorBooks().add(new AuthorBook(author, book)));
		};
		return book;
	}

	// Callers must guard for null — a null publisher means "keep/unset", never "look it up".
	private Publisher resolvePublisher(UUID publisherId) {
		return publisherRepository
				       .findById(publisherId)
				       .orElseThrow(() -> new ResourceNotFoundException("Publisher not found"));
	}

	private List<Author> resolveAuthors(List<UUID> authorIds) {
		List<Author> authors = authorRepository.findAllById(authorIds);
		if (authors.size() != authorIds.size()) {
			throw new ResourceNotFoundException("One or more authors not found");
		}
		return authors;
	}

	// Update semantics (see README "Omitted vs. empty vs. explicit"):
	// - omitted/null authorIds -> keep the current authors
	// - empty list -> clear all author links
	// - non-empty list -> replace with exactly these authors; unknown ids are rejected
	private void syncAuthors(Book book, List<UUID> newAuthorIds) {
		if (newAuthorIds == null) {
			return;
		}

		// Resolve all desired authors in one batch query; fails if any id is unknown
		List<Author> desiredAuthors = resolveAuthors(newAuthorIds.stream().distinct().toList());

		// Remove links to authors that are no longer in the request
		Set<UUID> desired = desiredAuthors.stream().map(Author::getId).collect(Collectors.toSet());
		book.getAuthorBooks().removeIf(ab -> !desired.contains(ab.getAuthor().getId()));

		// Add links for authors not yet on the book
		Set<UUID> current =
				book.getAuthorBooks().stream().map(ab -> ab.getAuthor().getId()).collect(Collectors.toSet());
		desiredAuthors.forEach(author -> {
			if (!current.contains(author.getId())) {
				book.getAuthorBooks().add(new AuthorBook(author, book));
			}
		});
	}
}
