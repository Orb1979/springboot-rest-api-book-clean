package org.example.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
This is a plain unit test of the DTO Bean Validation constraints.
No Spring context is loaded — jakarta.validation.Validator is used directly.

It covers every constraint declared on the request DTOs
so the @WebMvcTest slices only need one representative test per endpoint
(the wiring of @Valid -> 400 ProblemDetail) instead of one test per constraint.
*/

class DtoValidationTest {

  private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private static Set<String> violations(Object dto) {
    return validator.validate(dto).stream()
        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
        .collect(Collectors.toSet());
  }

  // --- AuthorRequestDto ---

  @Test
  void authorRequestDtoIsValid() {
    AuthorRequest dto = new AuthorRequest("John", "Doe", LocalDate.of(1990, 1, 1));
    assertEquals(Set.of(), violations(dto));
  }

  @Test
  void authorRequestDtoRejectsBlankNames() {
    AuthorRequest dto = new AuthorRequest("", "  ", LocalDate.of(1990, 1, 1));
    assertEquals(Set.of("firstName: must not be blank", "lastName: must not be blank"), violations(dto));
  }

  @Test
  void authorRequestDtoRejectsOversizedNames() {
    AuthorRequest dto = new AuthorRequest("a".repeat(101), "b".repeat(101), null);
    assertEquals(
        Set.of(
            "firstName: size must be between 0 and 100",
            "lastName: size must be between 0 and 100"),
        violations(dto));
  }

  @Test
  void authorRequestDtoRejectsFutureBirthDate() {
    AuthorRequest dto = new AuthorRequest("John", "Doe", LocalDate.now().plusDays(1));
    assertEquals(Set.of("birthDate: must be a past date"), violations(dto));
  }

  // --- PublisherRequestDto ---

  @Test
  void publisherRequestDtoIsValid() {
    PublisherRequest dto = new PublisherRequest("Manning");
    assertEquals(Set.of(), violations(dto));
  }

  @Test
  void publisherRequestDtoRejectsBlankName() {
    PublisherRequest dto = new PublisherRequest("  ");
    assertEquals(Set.of("name: must not be blank"), violations(dto));
  }

  @Test
  void publisherRequestDtoRejectsOversizedName() {
    PublisherRequest dto = new PublisherRequest("a".repeat(101));
    assertEquals(Set.of("name: size must be between 0 and 100"), violations(dto));
  }

  // --- BookRequestDto ---

  @Test
  void bookRequestDtoIsValid() {
    BookRequest dto =
        new BookRequest(
            "title",
            "subtitle",
            "description",
            100,
            "isbn",
            UUID.randomUUID(),
            List.of(UUID.randomUUID()));
    assertEquals(Set.of(), violations(dto));
  }

  @Test
  void bookRequestDtoRejectsBlankTitleAndSubTitle() {
    BookRequest dto = new BookRequest("", " ", "description", 100, "isbn", null, null);
    assertEquals(Set.of("title: must not be blank", "subTitle: must not be blank"), violations(dto));
  }

  @Test
  void bookRequestDtoRejectsOversizedTitle() {
    BookRequest dto = new BookRequest("t".repeat(256), "subtitle", "description", 100, "isbn", null, null);
    assertEquals(Set.of("title: size must be between 0 and 255"), violations(dto));
  }

  @Test
  void bookRequestDtoRejectsNonPositivePages() {
    BookRequest dto = new BookRequest("title", "subtitle", "description", 0, "isbn", null, null);
    assertEquals(Set.of("pages: must be greater than 0"), violations(dto));
  }

  @Test
  void bookRequestDtoRejectsOversizedIsbn() {
    BookRequest dto = new BookRequest("title", "subtitle", "description", 100, "i".repeat(21), null, null);
    assertEquals(Set.of("isbn: size must be between 0 and 20"), violations(dto));
  }
}
