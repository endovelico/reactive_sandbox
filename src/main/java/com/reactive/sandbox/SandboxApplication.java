package com.reactive.sandbox;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SandboxApplication {

	public static void main(String[] args) {
		SpringApplication.run(SandboxApplication.class, args);
	}

	@Bean
	public CommandLineRunner runner() {
		return args -> {
			System.out.println("This runs AFTER Spring Boot has started");
		};
	}
}
