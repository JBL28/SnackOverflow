package com.snackoverflow;

import org.springframework.boot.SpringApplication;

public class TestSnackoverflowApplication {

	public static void main(String[] args) {
		SpringApplication.from(SnackoverflowApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
