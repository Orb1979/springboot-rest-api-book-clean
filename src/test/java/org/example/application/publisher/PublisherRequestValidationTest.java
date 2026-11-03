package org.example.application.publisher;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
Plain unit test of the PublisherRequest Bean Validation constraints.
No Spring context is loaded — jakarta.validation.Validator is used directly,
so the @WebMvcTest slices only need one representative test per endpoint
(the wiring of @Valid -> 400 ProblemDetail) instead of one test per constraint.
*/
class PublisherRequestValidationTest {

  private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private static Set<String> violations(Object dto) {
    return validator.validate(dto).stream()
        .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
        .collect(Collectors.toSet());
  }

  @Test
  void publisherRequestIsValid() {
    PublisherRequest dto = new PublisherRequest("Manning");
    assertEquals(Set.of(), violations(dto));
  }

  @Test
  void publisherRequestRejectsBlankName() {
    PublisherRequest dto = new PublisherRequest("  ");
    assertEquals(Set.of("name: must not be blank"), violations(dto));
  }

  @Test
  void publisherRequestRejectsOversizedName() {
    PublisherRequest dto = new PublisherRequest("a".repeat(101));
    assertEquals(Set.of("name: size must be between 0 and 100"), violations(dto));
  }
}
