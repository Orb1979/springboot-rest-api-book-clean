package org.example.application.author;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
Plain unit test of the AuthorRequest Bean Validation constraints.
No Spring context is loaded — jakarta.validation.Validator is used directly,
so the @WebMvcTest slices only need one representative test per endpoint
(the wiring of @Valid -> 400 ProblemDetail) instead of one test per constraint.
*/
class AuthorRequestValidationTest {

  private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private static Set<String> violations(Object dto) {
    return validator.validate(dto).stream()
        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
        .collect(Collectors.toSet());
  }

  @Test
  void authorRequestIsValid() {
    AuthorRequest dto = new AuthorRequest("John", "Doe", LocalDate.of(1990, 1, 1));
    assertEquals(Set.of(), violations(dto));
  }

  @Test
  void authorRequestRejectsBlankNames() {
    AuthorRequest dto = new AuthorRequest("", "  ", LocalDate.of(1990, 1, 1));
    assertEquals(Set.of("firstName: must not be blank", "lastName: must not be blank"), violations(dto));
  }

  @Test
  void authorRequestRejectsOversizedNames() {
    AuthorRequest dto = new AuthorRequest("a".repeat(101), "b".repeat(101), null);
    assertEquals(
        Set.of(
            "firstName: size must be between 0 and 100",
            "lastName: size must be between 0 and 100"),
        violations(dto));
  }

  @Test
  void authorRequestRejectsFutureBirthDate() {
    AuthorRequest dto = new AuthorRequest("John", "Doe", LocalDate.now().plusDays(1));
    assertEquals(Set.of("birthDate: must be a past date"), violations(dto));
  }
}
