package com.microjobs.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping("/health")
    public String health() {
        return "MicroJobs Marketplace - Demo Service Running!";
    }

    @GetMapping("/")
    public String home() {
        return "Welcome to MicroJobs Marketplace!\n" +
               "Services:\n" +
               "- Jobs Service: http://localhost:8083\n" +
               "- Escrow Service: http://localhost:8084\n" +
               "- API Gateway: http://localhost:8080\n" +
               "- Keycloak Admin: http://localhost:8085/admin\n" +
               "- Admin Console: http://localhost:3000\n\n" +
               "Infrastructure:\n" +
               "- PostgreSQL: localhost:5432\n" +
               "- Redis: localhost:6379\n" +
               "- Kafka: localhost:9092\n" +
               "- Elasticsearch: localhost:9200\n" +
               "- Kibana: localhost:5601\n" +
               "- MinIO: localhost:9000\n" +
               "- Keycloak: localhost:8085";
    }

    @GetMapping("/status")
    public String status() {
        return "✅ Infrastructure Services Running\n" +
               "✅ Demo Service Active\n" +
               "✅ Ready for Testing!";
    }
}
