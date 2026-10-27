package org.example.opencodelearntest;

import org.springframework.boot.SpringApplication;

public class TestOpenCodeLearnTestApplication {

	public static void main(String[] args) {
		SpringApplication.from(OpenCodeLearnTestApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
