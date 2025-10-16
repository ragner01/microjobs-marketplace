package com.microjobs.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class
})
@EntityScan(basePackages = {
    "com.microjobs.search.domain",
    "com.microjobs.shared.domain"
})
@EnableJpaRepositories(basePackages = {
    "com.microjobs.search.ports"
})
@EnableJpaAuditing
@ComponentScan(basePackages = {
    "com.microjobs.search",
    "com.microjobs.shared.infrastructure.security"
})
public class SearchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SearchServiceApplication.class, args);
    }
}
