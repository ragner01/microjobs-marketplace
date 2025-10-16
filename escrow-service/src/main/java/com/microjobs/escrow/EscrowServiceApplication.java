package com.microjobs.escrow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication(exclude = {
    org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class
})
@EntityScan(basePackages = {
    "com.microjobs.escrow.domain",
    "com.microjobs.shared.domain"
})
@EnableJpaRepositories(basePackages = {
    "com.microjobs.escrow.ports"
})
@EnableJpaAuditing
public class EscrowServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EscrowServiceApplication.class, args);
    }
}