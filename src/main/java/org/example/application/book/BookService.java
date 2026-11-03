package org.example.application.book;

import lombok.RequiredArgsConstructor;
import org.example.application.dto.AuthorResponse;
import org.example.application.dto.BookRequest;
import org.example.application.dto.BookResponse;
import org.example.application.dto.PublisherResponse;
import org.example.application.exception.BadRequestException;
import org.example.application.exception.ResourceNotFoundException;
import org.example.domain.Author;
import org.example.domain.AuthorRepository;
import org.example.domain.Book;
import org.example.domain.BookRepository;
import org.example.domain.Publisher;
import org.example.domain.PublisherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService implements BookUseCase {
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

	public BookResponse createBook(BookRequest bookRequest) {
		Book book = new Book();
		book.setTitle(bookRequest.title());
		book.setSubTitle(bookRequest.subTitle());
		book.setDescription(bookRequest.description());
		book.setPages(bookRequest.pages());
		book.setIsbn(bookRequest.isbn());
		if (bookRequest.publisherId() != null) {
			book.setPublisher(resolvePublisher(bookRequest.publisherId()));
		}
		if (bookRequest.authorIds() != null) {
			resolveAuthors(bookRequest.authorIds()).forEach(book::addAuthor);
		}
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
		// Omitted authorIds keeps the current authors; an empty list clears them
		if (bookRequestDto.authorIds() != null) {
			existingBook.replaceAuthors(
					resolveAuthors(bookRequestDto.authorIds().stream().distinct().toList()));
		}

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

		if (book.addAuthor(author)) {
			bookRepository.save(book);
		}
		return getBookById(bookId);
	}

	@Transactional
	public BookResponse removeAuthorFromBook(UUID bookId, UUID authorId) {
		Book book = bookRepository.findById(bookId)
						.orElseThrow(() -> new ResourceNotFoundException("Book not found"));

		book.removeAuthor(authorId);
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

		book.replaceAuthors(resolveAuthors(authorIds.stream().distinct().toList()));

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
				book.getAuthors().stream()
						.map(author -> new AuthorResponse(
								author.getId(),
								author.getFirstName(),
								author.getLastName(),
								author.getBirthDate()
						)).toList()
		);
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
}
