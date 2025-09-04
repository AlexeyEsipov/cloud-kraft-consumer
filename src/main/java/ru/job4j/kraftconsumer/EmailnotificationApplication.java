package ru.job4j.kraftconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EmailnotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailnotificationApplication.class, args);
	}
}
