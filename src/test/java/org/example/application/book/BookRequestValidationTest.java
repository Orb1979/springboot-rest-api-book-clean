package org.example.application.book;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
Plain unit test of the BookRequest Bean Validation constraints.
No Spring context is loaded — jakarta.validation.Validator is used directly,
so the @WebMvcTest slices only need one representative test per endpoint
(the wiring of @Valid -> 400 ProblemDetail) instead of one test per constraint.
*/
class BookRequestValidationTest {

  private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private static Set<String> violations(Object dto) {
    return validator.validate(dto).stream()
        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
        .collect(Collectors.toSet());
  }

  @Test
  void bookRequestIsValid() {
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
  void bookRequestRejectsBlankTitleAndSubTitle() {
    BookRequest dto = new BookRequest("", " ", "description", 100, "isbn", null, null);
    assertEquals(Set.of("title: must not be blank", "subTitle: must not be blank"), violations(dto));
  }

  @Test
  void bookRequestRejectsOversizedTitle() {
    BookRequest dto = new BookRequest("t".repeat(256), "subtitle", "description", 100, "isbn", null, null);
    assertEquals(Set.of("title: size must be between 0 and 255"), violations(dto));
  }

  @Test
  void bookRequestRejectsNonPositivePages() {
    BookRequest dto = new BookRequest("title", "subtitle", "description", 0, "isbn", null, null);
    assertEquals(Set.of("pages: must be greater than 0"), violations(dto));
  }

  @Test
  void bookRequestRejectsOversizedIsbn() {
    BookRequest dto = new BookRequest("title", "subtitle", "description", 100, "i".repeat(21), null, null);
    assertEquals(Set.of("isbn: size must be between 0 and 20"), violations(dto));
  }
}
