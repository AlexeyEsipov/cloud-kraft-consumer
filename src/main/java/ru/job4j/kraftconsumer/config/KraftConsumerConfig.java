package ru.job4j.kraftconsumer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class KraftConsumerConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
