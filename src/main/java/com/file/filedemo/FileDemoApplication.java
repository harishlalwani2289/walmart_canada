package com.file.filedemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class FileDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileDemoApplication.class, args);
	}

}
