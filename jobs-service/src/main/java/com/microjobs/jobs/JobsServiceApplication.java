package com.microjobs.jobs;

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
    "com.microjobs.jobs.domain",
    "com.microjobs.shared.domain"
})
@EnableJpaRepositories(basePackages = {
    "com.microjobs.jobs.ports"
})
@EnableJpaAuditing
@ComponentScan(basePackages = {
    "com.microjobs.jobs",
    "com.microjobs.shared.infrastructure.security"
})
public class JobsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobsServiceApplication.class, args);
    }
}