package org.example.web;

import org.example.dto.AuthorResponse;
import org.example.dto.BookRequest;
import org.example.dto.BookResponse;
import org.example.dto.PublisherResponse;
import org.example.exception.ResourceNotFoundException;
import org.example.service.BookService;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@Import(ApiExceptionHandler.class)
public class BookControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private BookService bookService;

	private UUID bookId;
	private UUID publisherId;
	private UUID authorId;

	@BeforeEach
	void setUp() {
		bookId = UUID.randomUUID();
		publisherId = UUID.randomUUID();
		authorId = UUID.randomUUID();
	}

	@Test
	void getAllBooksReturnsBooks() throws Exception {
		BookResponse book = createBookResponseDto(bookId, publisherId, authorId);

		when(bookService.getAllBooks()).thenReturn(List.of(book));

		mockMvc
				.perform(get("/api/books"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(bookId.toString()))
				.andExpect(jsonPath("$[0].title").value("title"))
				.andExpect(jsonPath("$[0].publisher.id").value(publisherId.toString()))
				.andExpect(jsonPath("$[0].authors[0].id").value(authorId.toString()));
	}

	@Test
	void getAllBooksReturnsEmptyList() throws Exception {
		when(bookService.getAllBooks()).thenReturn(List.of());

		mockMvc
				.perform(get("/api/books"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void getBookByIdReturnsBook() throws Exception {
		BookResponse book = createBookResponseDto(bookId, publisherId, authorId);

		when(bookService.getBookById(bookId)).thenReturn(book);

		mockMvc
				.perform(get("/api/books/{id}", bookId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(bookId.toString()))
				.andExpect(jsonPath("$.title").value("title"))
				.andExpect(jsonPath("$.subTitle").value("subTitle"))
				.andExpect(jsonPath("$.description").value("description"))
				.andExpect(jsonPath("$.pages").value(100))
				.andExpect(jsonPath("$.isbn").value("isbn-123456"))
				.andExpect(jsonPath("$.publisher.id").value(publisherId.toString()))
				.andExpect(jsonPath("$.publisher.name").value("publisher"))
				.andExpect(jsonPath("$.authors[0].id").value(authorId.toString()))
				.andExpect(jsonPath("$.authors[0].firstName").value("John"))
				.andExpect(jsonPath("$.authors[0].lastName").value("Doe"));
	}

	@Test
	void getBookByIdReturnsNotFound() throws Exception {
		when(bookService.getBookById(bookId))
				.thenThrow(new ResourceNotFoundException("Book not found, id: " + bookId));

		mockMvc
				.perform(get("/api/books/{id}", bookId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Book not found, id: " + bookId));
	}

	@Test
	void createBookReturnsCreatedBook() throws Exception {
		String requestJson = toRequestJson("New Book", "Sub", "desc", 123, "isbn-2");
		BookResponse created =
				createBookResponseDto(
						bookId,
						"New Book",
						"Sub",
						"desc",
						123,
						"isbn-2",
						publisherId,
						"Publisher",
						authorId,
						"John",
						"Doe",
						null);

		when(bookService.createBook(any(BookRequest.class))).thenReturn(created);

		mockMvc
				.perform(post("/api/books").contentType("application/json").content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(bookId.toString()))
				.andExpect(jsonPath("$.title").value("New Book"))
				.andExpect(jsonPath("$.publisher.id").value(publisherId.toString()))
				.andExpect(jsonPath("$.authors[0].id").value(authorId.toString()));
	}

	@Test
	void createBookRejectsMissingTitle() throws Exception {
		String requestJson = toRequestJson("", "Sub", "desc", 123, "isbn-2");

		mockMvc
				.perform(post("/api/books").contentType("application/json").content(requestJson))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detail").value("title: must not be blank"));

		verify(bookService, never()).createBook(any());
	}

	@Test
	void updateBookReturnsUpdatedBook() throws Exception {
		String requestJson = toRequestJson("Updated Book", "Sub", "desc", 200, "isbn");
		BookResponse updated =
				createBookResponseDto(
						bookId,
						"Updated Book",
						"Sub",
						"desc",
						200,
						"isbn",
						publisherId,
						"Publisher",
						authorId,
						"Jane",
						"Smith",
						null);

		when(bookService.updateBook(eq(bookId), any(BookRequest.class))).thenReturn(updated);

		mockMvc
				.perform(
						put("/api/books/{id}", bookId).contentType("application/json").content(requestJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(bookId.toString()))
				.andExpect(jsonPath("$.title").value("Updated Book"))
				.andExpect(jsonPath("$.publisher.id").value(publisherId.toString()))
				.andExpect(jsonPath("$.authors[0].id").value(authorId.toString()));
	}

	@Test
	void updateBookReturnsNotFound() throws Exception {
		String requestJson = toRequestJson("Updated Book", "Sub", "desc", 200, "isbn");

		when(bookService.updateBook(eq(bookId), any(BookRequest.class)))
				.thenThrow(new ResourceNotFoundException("Book not found, id: " + bookId));

		mockMvc
				.perform(
						put("/api/books/{id}", bookId).contentType("application/json").content(requestJson))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Book not found, id: " + bookId));
	}

	@Test
	void deleteBookReturnsOk() throws Exception {
		doNothing().when(bookService).deleteBook(bookId);

		mockMvc.perform(delete("/api/books/{id}", bookId)).andExpect(status().isNoContent());

		verify(bookService).deleteBook(bookId);
	}

	@Test
	void deleteBookReturnsNotFound() throws Exception {
		doThrow(new ResourceNotFoundException("Book not found, id: " + bookId))
				.when(bookService)
				.deleteBook(bookId);

		mockMvc
				.perform(delete("/api/books/{id}", bookId))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Book not found, id: " + bookId));
	}

	// --- Granular Sub-Resource Endpoints ---

	@Test
	void addAuthorToBookReturnsBook() throws Exception {
		BookResponse book = createBookResponseDto(bookId, publisherId, authorId);
		when(bookService.addAuthorToBook(bookId, authorId)).thenReturn(book);

		mockMvc
				.perform(put("/api/books/{id}/authors/{authorId}", bookId, authorId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(bookId.toString()))
				.andExpect(jsonPath("$.authors[0].id").value(authorId.toString()));

		verify(bookService).addAuthorToBook(bookId, authorId);
	}

	@Test
	void removeAuthorFromBookReturnsBook() throws Exception {
		BookResponse book = createBookResponseDto(bookId, publisherId, authorId);
		when(bookService.removeAuthorFromBook(bookId, authorId)).thenReturn(book);

		mockMvc
				.perform(delete("/api/books/{id}/authors/{authorId}", bookId, authorId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(bookId.toString()));

		verify(bookService).removeAuthorFromBook(bookId, authorId);
	}

	@Test
	void removePublisherFromBookReturnsBook() throws Exception {
		BookResponse book = createBookResponseDto(bookId, publisherId, authorId);
		when(bookService.removePublisherFromBook(bookId)).thenReturn(book);

		mockMvc
				.perform(delete("/api/books/{id}/publisher", bookId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(bookId.toString()));

		verify(bookService).removePublisherFromBook(bookId);
	}

	// --- Conflict Handling (database constraint violations) ---

	@Test
	void createBookReturnsConflictOnDuplicateIsbn() throws Exception {
		String requestJson = toRequestJson("New Book", "Sub", "desc", 123, "isbn-2");

		when(bookService.createBook(any(BookRequest.class)))
				.thenThrow(duplicateConstraintViolation("book_isbn_key"));

		mockMvc
				.perform(post("/api/books").contentType("application/json").content(requestJson))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("A book with this ISBN already exists"));
	}

	@Test
	void addAuthorToBookReturnsConflictOnDuplicateAssociation() throws Exception {
		when(bookService.addAuthorToBook(bookId, authorId))
				.thenThrow(duplicateConstraintViolation("uq_author_book"));

		mockMvc
				.perform(put("/api/books/{id}/authors/{authorId}", bookId, authorId))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("This author is already associated with this book"));
	}

	@Test
	void createBookReturnsGenericConflictForUnknownConstraint() throws Exception {
		String requestJson = toRequestJson("New Book", "Sub", "desc", 123, "isbn-2");

		when(bookService.createBook(any(BookRequest.class)))
				.thenThrow(new DataIntegrityViolationException("violated some constraint"));

		mockMvc
				.perform(post("/api/books").contentType("application/json").content(requestJson))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.detail").value("The request conflicts with existing data"));
	}

	// --- Helper Methods ---

	// Simulates what Spring wraps when a unique constraint fires at flush time:
	// a Hibernate ConstraintViolationException naming the violated constraint.
	private DataIntegrityViolationException duplicateConstraintViolation(String constraintName) {
		var hibernate =
				new ConstraintViolationException("duplicate key", null, constraintName);
		return new DataIntegrityViolationException("could not execute statement", hibernate);
	}

	private String toRequestJson(
			String title, String subTitle, String description, int pages, String isbn) {
		return """
        {
          "title":"%s",
          "subTitle":"%s",
          "description":"%s",
          "pages":%d,
          "isbn":"%s",
          "publisherId":"%s",
          "authorIds":["%s"]
        }
        """.formatted(title, subTitle, description, pages, isbn, publisherId, authorId);
	}

	private BookResponse createBookResponseDto(
			UUID bookId,
			String title,
			String subTitle,
			String description,
			int pages,
			String isbn,
			UUID publisherId,
			String publisherName,
			UUID authorId,
			String authorFirstName,
			String authorLastName,
			LocalDate authorBirthDate) {
		return new BookResponse(
				bookId,
				title,
				subTitle,
				description,
				pages,
				isbn,
				new PublisherResponse(publisherId, publisherName),
				List.of(new AuthorResponse(authorId, authorFirstName, authorLastName, authorBirthDate)));
	}

	private BookResponse createBookResponseDto(UUID bookId, UUID publisherId, UUID authorId) {
		return createBookResponseDto(
				bookId,
				"title",
				"subTitle",
				"description",
				100,
				"isbn-123456",
				publisherId,
				"publisher",
				authorId,
				"John",
				"Doe",
				null);
	}
}