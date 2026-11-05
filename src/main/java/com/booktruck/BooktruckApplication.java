package com.booktruck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BooktruckApplication {

	public static void main(String[] args) {
		SpringApplication.run(BooktruckApplication.class, args);
	}

}
