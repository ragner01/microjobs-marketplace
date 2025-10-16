# MicroJobs Marketplace

A production-ready, multi-tenant micro-jobs marketplace built with Spring Boot, featuring Domain-Driven Design (DDD), Hexagonal Architecture, and Event Sourcing.

## 🏗️ Architecture

- **Domain-Driven Design (DDD)** with Aggregates, Value Objects, and Domain Events
- **Hexagonal Architecture** (Ports & Adapters) for clean separation of concerns
- **Event Sourcing** with Kafka for reliable event-driven communication
- **CQRS** (Command Query Responsibility Segregation) pattern
- **Multi-tenant** architecture with schema-per-tenant isolation
- **Microservices** architecture with Spring Cloud Gateway

## 🚀 Features

### Core Services
- **Jobs Service**: Job posting, bidding, and assignment management
- **Escrow Service**: Financial transactions, wallet management, and dispute resolution
- **Bids & Matching Service**: Intelligent job-worker matching algorithms
- **Disputes Service**: Conflict resolution and arbitration workflows
- **Payments Service**: Integration with Paystack and Stripe payment providers
- **Notifications Service**: Real-time notifications via WebSocket/STOMP
- **Search Service**: Full-text search with Elasticsearch
- **Analytics Service**: Business intelligence and reporting

### Infrastructure
- **PostgreSQL**: Multi-tenant database with schema isolation
- **Redis**: Caching and rate limiting
- **Kafka**: Event streaming and message queuing
- **Elasticsearch**: Full-text search and analytics
- **MinIO/S3**: Object storage for files and documents
- **Keycloak**: OAuth2/OIDC authentication and authorization
- **Spring Cloud Gateway**: API Gateway with routing and load balancing

### Production Features
- **Resilience4j**: Circuit breakers, retries, and bulkheads
- **OpenTelemetry**: Distributed tracing and observability
- **Prometheus + Grafana**: Metrics collection and visualization
- **Docker Compose**: Local development environment
- **Kubernetes Helm Charts**: Production deployment
- **GitHub Actions**: CI/CD pipeline
- **Testcontainers**: Integration testing

## 🛠️ Technology Stack

- **Java 11** with Spring Boot 2.7.18
- **Spring Data JPA** with Hibernate
- **Spring Security** with OAuth2/JWT
- **Spring Cloud** (Gateway, OpenFeign, Circuit Breaker)
- **Apache Kafka** for event streaming
- **PostgreSQL** for data persistence
- **Redis** for caching
- **Elasticsearch** for search
- **Docker** and **Kubernetes** for containerization
- **Gradle** for build automation

## 🚀 Quick Start

### Prerequisites
- Java 11+
- Docker and Docker Compose
- Gradle 7+

### 1. Clone and Setup
```bash
git clone https://github.com/YOUR_USERNAME/microjobs-marketplace.git
cd microjobs-marketplace
```

### 2. Start Infrastructure
```bash
make local-infra
```

### 3. Start Services
```bash
./start-for-testSprite.sh
```

### 4. Access Services
- **Demo Dashboard**: http://localhost:8086/dashboard
- **Jobs API**: http://localhost:8083/api/jobs
- **Escrow API**: http://localhost:8084/api/escrow
- **API Gateway**: http://localhost:8080
- **Keycloak Admin**: http://localhost:8085/admin

## 📊 API Documentation

### Jobs Service
- `POST /api/jobs` - Create a new job
- `GET /api/jobs/{id}` - Get job details
- `GET /api/jobs` - List jobs with filtering
- `POST /api/jobs/{jobId}/bids` - Submit a bid
- `PUT /api/jobs/{jobId}/assign/{bidId}` - Assign job to worker
- `PUT /api/jobs/{jobId}/complete` - Mark job as completed

### Escrow Service
- `POST /api/escrow/accounts` - Create escrow account
- `GET /api/escrow/accounts/{id}` - Get account details
- `POST /api/escrow/accounts/{accountId}/deposit` - Deposit funds
- `POST /api/escrow/accounts/{accountId}/withdraw` - Withdraw funds
- `POST /api/escrow/transactions` - Create escrow transaction
- `PUT /api/escrow/transactions/{id}/complete` - Complete transaction

## 🧪 Testing

The project includes comprehensive testing setup:

- **Unit Tests**: JUnit 5 with Mockito
- **Integration Tests**: Testcontainers for database testing
- **Contract Tests**: Pact for service contracts
- **Load Tests**: Gatling for performance testing
- **TestSprite Integration**: Ready for automated API testing

## 🚀 Deployment

### Local Development
```bash
# Start all services
make local-infra
make start-services

# Run tests
make test
```

### Production Deployment
```bash
# Build Docker images
make build-images

# Deploy to Kubernetes
helm install microjobs ./helm/microjobs-marketplace
```

## 📈 Monitoring

- **Prometheus**: Metrics collection at `/actuator/prometheus`
- **Grafana**: Dashboards for system monitoring
- **Jaeger**: Distributed tracing
- **ELK Stack**: Centralized logging

## 🔒 Security

- **OAuth2/JWT**: Token-based authentication
- **RBAC/ABAC**: Role and attribute-based access control
- **Multi-tenant isolation**: Schema-per-tenant security
- **Data encryption**: At rest and in transit
- **Audit trails**: Comprehensive logging and monitoring

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🎯 Roadmap

- [ ] **Phase 1**: Core marketplace functionality ✅
- [ ] **Phase 2**: Advanced matching algorithms
- [ ] **Phase 3**: Machine learning recommendations
- [ ] **Phase 4**: Mobile applications
- [ ] **Phase 5**: International expansion features

## 📞 Support

- **Documentation**: [Wiki](https://github.com/YOUR_USERNAME/microjobs-marketplace/wiki)
- **Issues**: [GitHub Issues](https://github.com/YOUR_USERNAME/microjobs-marketplace/issues)
- **Discussions**: [GitHub Discussions](https://github.com/YOUR_USERNAME/microjobs-marketplace/discussions)

## 🌟 Features Highlights

- ✅ **Production Ready**: Comprehensive error handling, monitoring, and observability
- ✅ **Scalable**: Microservices architecture with horizontal scaling
- ✅ **Secure**: Multi-tenant security with OAuth2/JWT
- ✅ **Reliable**: Event sourcing with outbox pattern for guaranteed delivery
- ✅ **Observable**: Full tracing, metrics, and logging
- ✅ **Testable**: Comprehensive test suite with Testcontainers
- ✅ **Deployable**: Docker and Kubernetes ready

---

**Built with ❤️ using Spring Boot, Domain-Driven Design, and modern microservices patterns.**