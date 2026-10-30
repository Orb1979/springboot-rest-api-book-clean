package org.example.web;

import org.example.exception.BadRequestException;
import org.example.exception.ResourceNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ProblemDetail handleResourceNotFound(ResourceNotFoundException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(BadRequestException.class)
	public ProblemDetail handleBadException(BadRequestException exception) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	// Translates database constraint violations into 409 responses
	// (without leaking constraint or schema details to the client)
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException exception) {
		String detail = "The request conflicts with existing data";

		if (exception.getCause() instanceof ConstraintViolationException hibernate) {
			detail = switch (hibernate.getConstraintName() == null ? "" : hibernate.getConstraintName()) {
				case "book_isbn_key" -> "A book with this ISBN already exists";
				case "uq_author_book" -> "This author is already associated with this book";
				default -> detail;
			};
		}
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, detail);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
		String detail = exception.getBindingResult().getFieldErrors().stream()
						.map(error -> error.getField() + ": " + error.getDefaultMessage())
						.collect(Collectors.joining(", "));
		return ProblemDetail.forStatusAndDetail(
				HttpStatus.BAD_REQUEST, detail.isBlank() ? "request validation failed" : detail);
	}

}
