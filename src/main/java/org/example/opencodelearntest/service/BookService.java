package org.example.opencodelearntest.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.opencodelearntest.dto.AuthorResponse;
import org.example.opencodelearntest.dto.BookRequest;
import org.example.opencodelearntest.dto.BookResponse;
import org.example.opencodelearntest.dto.PublisherResponse;
import org.example.opencodelearntest.entity.Author;
import org.example.opencodelearntest.entity.AuthorBook;
import org.example.opencodelearntest.entity.Book;
import org.example.opencodelearntest.entity.Publisher;
import org.example.opencodelearntest.exception.ResourceNotFoundException;
import org.example.opencodelearntest.repository.AuthorRepository;
import org.example.opencodelearntest.repository.BookRepository;
import org.example.opencodelearntest.repository.PublisherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookService {
	private final BookRepository bookRepository;
	private final PublisherRepository publisherRepository;
	private final AuthorRepository authorRepository;

	public List<BookResponse> getAllBooks() {
		return bookRepository.findAllHydrated().stream().map(BookService::toResponseDto).toList();
	}

	public BookResponse getBookById(UUID id) {
		Book book = bookRepository.findByIdHydrated(id).orElseThrow(
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
						.findByIdHydrated(id)
						.orElseThrow(() -> new ResourceNotFoundException("Book not found, id: " + id));
		existingBook.setTitle(bookRequestDto.title());
		existingBook.setSubTitle(bookRequestDto.subTitle());
		existingBook.setDescription(bookRequestDto.description());
		existingBook.setPages(bookRequestDto.pages());
		existingBook.setIsbn(bookRequestDto.isbn());
		existingBook.setPublisher(resolvePublisher(bookRequestDto.publisherId()));
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

	private Publisher resolvePublisher(UUID publisherId) {
		if (publisherId == null) {
			return null;
		}
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
