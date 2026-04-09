package com.FinaSplitter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class FinaSplitterApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinaSplitterApplication.class, args);
	}

}
