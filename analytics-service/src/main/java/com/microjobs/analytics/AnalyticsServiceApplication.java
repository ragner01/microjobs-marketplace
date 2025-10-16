package com.microjobs.analytics;

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
    "com.microjobs.analytics.domain",
    "com.microjobs.shared.domain"
})
@EnableJpaRepositories(basePackages = {
    "com.microjobs.analytics.ports"
})
@EnableJpaAuditing
@ComponentScan(basePackages = {
    "com.microjobs.analytics",
    "com.microjobs.shared.infrastructure.security"
})
public class AnalyticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsServiceApplication.class, args);
    }
}
