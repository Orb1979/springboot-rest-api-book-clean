package org.example.service;

import org.example.dto.BookRequest;
import org.example.dto.BookResponse;
import org.example.entity.Author;
import org.example.entity.AuthorBook;
import org.example.entity.Book;
import org.example.entity.Publisher;
import org.example.exception.BadRequestException;
import org.example.exception.ResourceNotFoundException;
import org.example.repository.AuthorRepository;
import org.example.repository.BookRepository;
import org.example.repository.PublisherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  @Mock private BookRepository bookRepository;
  @Mock private PublisherRepository publisherRepository;
  @Mock private AuthorRepository authorRepository;
  @InjectMocks private BookService bookService;

  @Test
  void getAllBooksReturnsBooks() {
    Author author1 = new Author();
    author1.setId(UUID.randomUUID());

    Author author2 = new Author();
    author2.setId(UUID.randomUUID());

    Book book1 = new Book();
    book1.setId(UUID.randomUUID());
    book1.setPages(100);
    book1.getAuthorBooks().add(new AuthorBook(author1, book1));
    book1.getAuthorBooks().add(new AuthorBook(author2, book1));

    when(bookRepository.findAllWithDetailsBy()).thenReturn(List.of(book1));

    List<BookResponse> result = bookService.getAllBooks();
    assertEquals(1, result.size());
    assertEquals(2, result.getFirst().authors().size());
    assertEquals(book1.getId(), result.getFirst().id());
    assertEquals(author1.getId(), result.getFirst().authors().get(0).id());
    assertEquals(author2.getId(), result.getFirst().authors().get(1).id());
  }

  @Test
  void getAllBooksReturnsEmptyList() {
    when(bookRepository.findAllWithDetailsBy()).thenReturn(List.of());

    List<BookResponse> result = bookService.getAllBooks();

    assertEquals(0, result.size());
  }

  @Test
  void getBookByIdReturnsBook() {
    UUID bookId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();

    Author author = new Author();
    author.setId(authorId);

    Book book = new Book();
    book.setId(bookId);
    book.setTitle("title");
    book.setPages(0);
    book.getAuthorBooks().add(new AuthorBook(author, book));

    when(bookRepository.findWithDetailsById(bookId)).thenReturn(Optional.of(book));

    BookResponse result = bookService.getBookById(bookId);

    assertEquals(bookId, result.id());
    assertEquals("title", result.title());
    assertEquals(1, result.authors().size());
    assertEquals(authorId, result.authors().getFirst().id());
  }

  @Test
  void getBookByIdReturnsNotFound() {
    UUID bookId = UUID.randomUUID();
    when(bookRepository.findWithDetailsById(bookId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(bookId));
  }

  @Test
  void createBookReturnsCreatedBook() {
    UUID bookId = UUID.randomUUID();

    BookRequest request = new BookRequest("title", "subtitle", "description", 100, "isbn", null, null);

    when(bookRepository.save(any(Book.class)))
        .thenAnswer(
            invocation -> {
              Book savedBook = invocation.getArgument(0);
              savedBook.setId(bookId);
              return savedBook;
            });

    BookResponse result = bookService.createBook(request);
    assertNotNull(result);
    assertEquals(bookId, result.id());
    assertEquals("title", result.title());
    assertNull(result.publisher());
    assertEquals(0, result.authors().size());
    verify(bookRepository).save(any(Book.class));
  }

  @Test
  void createBookWithPublisherAndAuthors() {
    UUID bookId = UUID.randomUUID();
    UUID publisherId = UUID.randomUUID();
    UUID author1Id = UUID.randomUUID();
    UUID author2Id = UUID.randomUUID();

    Publisher publisher = new Publisher();
    publisher.setId(publisherId);

    Author author1 = new Author();
    author1.setId(author1Id);

    Author author2 = new Author();
    author2.setId(author2Id);

    BookRequest request =
        new BookRequest(
            "title",
            "subtitle",
            "description",
            100,
            "isbn",
            publisherId,
            List.of(author1Id, author2Id));

    when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(publisher));
    when(authorRepository.findAllById(List.of(author1Id, author2Id)))
        .thenReturn(List.of(author1, author2));
    when(bookRepository.save(any(Book.class)))
        .thenAnswer(
            invocation -> {
              Book savedBook = invocation.getArgument(0);
              savedBook.setId(bookId);
              return savedBook;
            });

    BookResponse result = bookService.createBook(request);
    assertNotNull(result);
    assertEquals(bookId, result.id());
    assertEquals(publisherId, result.publisher().id());
    assertEquals(2, result.authors().size());
    verify(bookRepository).save(any(Book.class));
  }

  @Test
  void createBookThrowsWhenAuthorNotFound() {
    UUID publisherId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();

    Publisher publisher = new Publisher();
    publisher.setId(publisherId);

    BookRequest request =
        new BookRequest("title", "subtitle", "description", 100, "isbn", publisherId, List.of(authorId));

    when(publisherRepository.findById(publisherId)).thenReturn(Optional.of(publisher));
    when(authorRepository.findAllById(List.of(authorId))).thenReturn(List.of());

    assertThrows(ResourceNotFoundException.class, () -> bookService.createBook(request));
    verify(bookRepository, never()).save(any());
  }

  @Test
  void createBookThrowsWhenPublisherNotFound() {
    UUID publisherId = UUID.randomUUID();

    BookRequest request = new BookRequest("title", "subtitle", "description", 100, "isbn", publisherId, List.of());

    when(publisherRepository.findById(publisherId)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> bookService.createBook(request));
    verify(bookRepository, never()).save(any());
  }

  @Test
  void updateBookReturnsUpdatedBook() {
    UUID bookId = UUID.randomUUID();
    UUID oldPublisherId = UUID.randomUUID();
    UUID newPublisherId = UUID.randomUUID();
    UUID oldAuthorId = UUID.randomUUID();
    UUID newAuthorId = UUID.randomUUID();

    Publisher oldPublisher = new Publisher();
    oldPublisher.setId(oldPublisherId);

    Publisher newPublisher = new Publisher();
    newPublisher.setId(newPublisherId);

    Author oldAuthor = new Author();
    oldAuthor.setId(oldAuthorId);

    Author newAuthor = new Author();
    newAuthor.setId(newAuthorId);

    Book existingBook = new Book();
    existingBook.setId(bookId);
    existingBook.setPublisher(oldPublisher);
    existingBook.getAuthorBooks().add(new AuthorBook(oldAuthor, existingBook));

    BookRequest request =
        new BookRequest(
            "New Title",
            "New Subtitle",
            "New Description",
            200,
            "new-isbn",
            newPublisherId,
            List.of(newAuthorId));

    when(bookRepository.findWithDetailsById(bookId)).thenReturn(Optional.of(existingBook));
    when(publisherRepository.findById(newPublisherId)).thenReturn(Optional.of(newPublisher));
    when(authorRepository.findAllById(List.of(newAuthorId))).thenReturn(List.of(newAuthor));
    when(bookRepository.save(existingBook)).thenReturn(existingBook);

    BookResponse result = bookService.updateBook(bookId, request);

    assertEquals("New Title", result.title());
    assertEquals(newPublisherId, result.publisher().id());
    assertEquals(1, result.authors().size());
    assertEquals(newAuthorId, result.authors().getFirst().id());
    verify(bookRepository).save(existingBook);
  }

  @Test
  void updateBookReturnsNotFound() {
    UUID id = UUID.randomUUID();
    UUID publisherId = UUID.randomUUID();
    BookRequest bookRequest = new BookRequest("title", "subTitle", "description", 100, "isbn", publisherId, List.of());
    when(bookRepository.findWithDetailsById(id)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> bookService.updateBook(id, bookRequest));
    verify(bookRepository, never()).save(any());
  }

  @Test
  void updateBookKeepsAuthorsWhenAuthorIdsOmitted() {
    UUID bookId = UUID.randomUUID();

    Author existingAuthor = new Author();
    existingAuthor.setId(UUID.randomUUID());

    Book existingBook = new Book();
    existingBook.setId(bookId);
    existingBook.setPages(100);
    existingBook.getAuthorBooks().add(new AuthorBook(existingAuthor, existingBook));

    BookRequest request = new BookRequest("title", "subtitle", "description", 100, "isbn", null, null);

    when(bookRepository.findWithDetailsById(bookId)).thenReturn(Optional.of(existingBook));
    when(bookRepository.save(existingBook)).thenReturn(existingBook);

    BookResponse result = bookService.updateBook(bookId, request);

    assertEquals(1, result.authors().size());
    assertEquals(existingAuthor.getId(), result.authors().getFirst().id());
    verify(bookRepository).save(existingBook);
  }

  @Test
  void updateBookClearsAuthorsWhenAuthorIdsEmpty() {
    UUID bookId = UUID.randomUUID();

    Author existingAuthor = new Author();
    existingAuthor.setId(UUID.randomUUID());

    Book existingBook = new Book();
    existingBook.setId(bookId);
    existingBook.setPages(100);
    existingBook.getAuthorBooks().add(new AuthorBook(existingAuthor, existingBook));

    BookRequest request = new BookRequest("title", "subtitle", "description", 100, "isbn", null, List.of());

    when(bookRepository.findWithDetailsById(bookId)).thenReturn(Optional.of(existingBook));
    when(authorRepository.findAllById(List.of())).thenReturn(List.of());
    when(bookRepository.save(existingBook)).thenReturn(existingBook);

    BookResponse result = bookService.updateBook(bookId, request);

    assertEquals(0, result.authors().size());
    assertTrue(existingBook.getAuthorBooks().isEmpty());
    verify(bookRepository).save(existingBook);
  }

  @Test
  void updateBookKeepsPublisherWhenPublisherIdOmitted() {
    UUID bookId = UUID.randomUUID();

    Publisher publisher = new Publisher();
    publisher.setId(UUID.randomUUID());

    Book existingBook = new Book();
    existingBook.setId(bookId);
    existingBook.setPages(100);
    existingBook.setPublisher(publisher);

    BookRequest request = new BookRequest("title", "subtitle", "description", 100, "isbn", null, null);

    when(bookRepository.findWithDetailsById(bookId)).thenReturn(Optional.of(existingBook));
    when(bookRepository.save(existingBook)).thenReturn(existingBook);

    BookResponse result = bookService.updateBook(bookId, request);

    assertEquals(publisher.getId(), result.publisher().id());
    verify(publisherRepository, never()).findById(any());
    verify(bookRepository).save(existingBook);
  }

  @Test
  void updateBookThrowsWhenAuthorNotFound() {
    UUID bookId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();

    Book existingBook = new Book();
    existingBook.setId(bookId);

    BookRequest request = new BookRequest("title", "subtitle", "description", 100, "isbn", null, List.of(authorId));

    when(bookRepository.findWithDetailsById(bookId)).thenReturn(Optional.of(existingBook));
    when(authorRepository.findAllById(List.of(authorId))).thenReturn(List.of());

    assertThrows(ResourceNotFoundException.class, () -> bookService.updateBook(bookId, request));
    verify(bookRepository, never()).save(any());
  }

  @Test
  void deleteBookReturnsOk() {
    UUID id = UUID.randomUUID();
    when(bookRepository.existsById(id)).thenReturn(true);
    bookService.deleteBook(id);
    verify(bookRepository).deleteById(id);
  }

  @Test
  void deleteBookReturnsNotFound() {
    UUID id = UUID.randomUUID();
    when(bookRepository.existsById(id)).thenReturn(false);
    assertThrows(ResourceNotFoundException.class, () -> bookService.deleteBook(id));
    verify(bookRepository, never()).deleteById(any());
  }

  // --- Granular Sub-Resource Operations ---

  @Test
  void addAuthorToBook() {
    UUID bookId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();

    Book book = new Book();
    book.setId(bookId);
    book.setPages(100);

    Author author = new Author();
    author.setId(authorId);

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
    when(bookRepository.findWithDetailsById(bookId)).thenReturn(Optional.of(book));

    BookResponse result = bookService.addAuthorToBook(bookId, authorId);

    assertNotNull(result);
    assertEquals(1, book.getAuthorBooks().size());
    verify(bookRepository).save(book);
    verify(bookRepository).findWithDetailsById(bookId);
  }

  @Test
  void addAuthorToBookIsIdempotentWhenAuthorAlreadyExist() {
    UUID bookId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();

    Author author = new Author();
    author.setId(authorId);

    Book book = new Book();
    book.setId(bookId);
    book.setPages(100);
    book.getAuthorBooks().add(new AuthorBook(author, book));

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    when(authorRepository.findById(authorId)).thenReturn(Optional.of(author));
    when(bookRepository.findWithDetailsById(bookId)).thenReturn(Optional.of(book));

    BookResponse result = bookService.addAuthorToBook(bookId, authorId);

    assertEquals(1, book.getAuthorBooks().size());
    assertEquals(1, result.authors().size());
    assertEquals(authorId, result.authors().getFirst().id());
    verify(bookRepository, never()).save(any());
  }

  @Test
  void removeAuthorFromBook() {
    UUID bookId = UUID.randomUUID();
    UUID authorId = UUID.randomUUID();

    Author author = new Author();
    author.setId(authorId);

    Book book = new Book();
    book.setId(bookId);
    book.setPages(100);
    book.getAuthorBooks().add(new AuthorBook(author, book));

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    when(bookRepository.findWithDetailsById(bookId)).thenReturn(Optional.of(book));

    BookResponse result = bookService.removeAuthorFromBook(bookId, authorId);

    assertNotNull(result);
    assertTrue(book.getAuthorBooks().isEmpty());
    verify(bookRepository).save(book);
    verify(bookRepository).findWithDetailsById(bookId);
  }

  @Test
  void removePublisherFromBook() {
    UUID bookId = UUID.randomUUID();
    Publisher publisher = new Publisher();
    publisher.setId(UUID.randomUUID());

    Book book = new Book();
    book.setId(bookId);
    book.setPages(100);
    book.setPublisher(publisher);

    when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
    when(bookRepository.findWithDetailsById(bookId)).thenReturn(Optional.of(book));

    BookResponse result = bookService.removePublisherFromBook(bookId);

    assertNotNull(result);
    assertNull(book.getPublisher());
    verify(bookRepository).save(book);
    verify(bookRepository).findWithDetailsById(bookId);
  }

  @Test
  void replaceAuthorsReplacesAuthorSet() {
    UUID bookId = UUID.randomUUID();
    UUID oldAuthorId = UUID.randomUUID();
    UUID newAuthorId = UUID.randomUUID();

    Author oldAuthor = new Author();
    oldAuthor.setId(oldAuthorId);

    Author newAuthor = new Author();
    newAuthor.setId(newAuthorId);

    Book book = new Book();
    book.setId(bookId);
    book.setPages(100);
    book.getAuthorBooks().add(new AuthorBook(oldAuthor, book));

    when(bookRepository.findWithDetailsById(bookId)).thenReturn(Optional.of(book));
    when(authorRepository.findAllById(List.of(newAuthorId))).thenReturn(List.of(newAuthor));
    when(bookRepository.save(book)).thenReturn(book);

    BookResponse result = bookService.replaceAuthors(bookId, List.of(newAuthorId));

    assertEquals(1, result.authors().size());
    assertEquals(newAuthorId, result.authors().getFirst().id());
    verify(bookRepository).save(book);
  }

  @Test
  void replaceAuthorsThrowsWhenAuthorIdsNull() {
    UUID bookId = UUID.randomUUID();

    assertThrows(BadRequestException.class, () -> bookService.replaceAuthors(bookId, null));

    verify(bookRepository, never()).save(any());
  }

  @Test
  void replaceAuthorsReturnsNotFound() {
    UUID bookId = UUID.randomUUID();
    when(bookRepository.findWithDetailsById(bookId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> bookService.replaceAuthors(bookId, List.of()));

    verify(bookRepository, never()).save(any());
  }
}
