package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PublisherRequest(
		@NotBlank @Size(max = 100) String name
) {}
