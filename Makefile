# MicroJobs Marketplace - Development Makefile

.PHONY: help build test clean docker-build docker-push k8s-deploy k8s-delete local-dev stop-local

# Default target
help: ## Show this help message
	@echo "MicroJobs Marketplace - Available Commands:"
	@echo "=========================================="
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# Build Commands
build: ## Build all modules
	@echo "Building all modules..."
	./gradlew clean build -x test

build-test: ## Build and run tests
	@echo "Building and testing all modules..."
	./gradlew clean build test

test: ## Run all tests
	@echo "Running tests..."
	./gradlew test

test-integration: ## Run integration tests
	@echo "Running integration tests..."
	./gradlew test --tests "*IntegrationTest"

clean: ## Clean build artifacts
	@echo "Cleaning build artifacts..."
	./gradlew clean
	docker system prune -f

# Docker Commands
docker-build: ## Build all Docker images
	@echo "Building Docker images..."
	./gradlew jibDockerBuild

docker-push: ## Push Docker images to registry
	@echo "Pushing Docker images..."
	./gradlew jib

docker-build-service: ## Build specific service (usage: make docker-build-service SERVICE=jobs-service)
	@echo "Building Docker image for $(SERVICE)..."
	./gradlew :$(SERVICE):jibDockerBuild

# Local Development
local-dev: ## Start local development environment
	@echo "Starting local development environment..."
	docker-compose up -d postgres redis zookeeper kafka elasticsearch kibana minio keycloak
	@echo "Waiting for services to be ready..."
	sleep 30
	@echo "Configuring Keycloak..."
	./docker/keycloak/configure-keycloak.sh
	@echo "Starting microservices..."
	./gradlew :api-gateway:bootRun &
	./gradlew :jobs-service:bootRun &
	./gradlew :escrow-service:bootRun &
	@echo "Local development environment started!"
	@echo "API Gateway: http://localhost:8080"
	@echo "Jobs Service: http://localhost:8083"
	@echo "Escrow Service: http://localhost:8084"
	@echo "Keycloak Admin: http://localhost:8085/admin"
	@echo "Admin Console: http://localhost:3000"

local-infra: ## Start only infrastructure services
	@echo "Starting infrastructure services..."
	docker-compose up -d postgres redis zookeeper kafka elasticsearch kibana minio keycloak
	@echo "Infrastructure services started!"
	@echo "PostgreSQL: localhost:5432"
	@echo "Redis: localhost:6379"
	@echo "Kafka: localhost:9092"
	@echo "Elasticsearch: localhost:9200"
	@echo "Kibana: localhost:5601"
	@echo "MinIO: localhost:9000"
	@echo "Keycloak: localhost:8085"

stop-local: ## Stop local development environment
	@echo "Stopping local development environment..."
	docker-compose down
	pkill -f "gradlew.*bootRun" || true

# Database Commands
db-migrate: ## Run database migrations
	@echo "Running database migrations..."
	./gradlew flywayMigrate

db-reset: ## Reset database (WARNING: This will delete all data)
	@echo "Resetting database..."
	docker-compose down postgres
	docker volume rm springboot-project_postgres_data || true
	docker-compose up -d postgres
	sleep 10
	make db-migrate

# Kubernetes Commands
k8s-deploy: ## Deploy to Kubernetes
	@echo "Deploying to Kubernetes..."
	kubectl apply -f k8s/
	@echo "Waiting for deployment to complete..."
	kubectl wait --for=condition=available --timeout=300s deployment/api-gateway -n microjobs

k8s-delete: ## Delete from Kubernetes
	@echo "Deleting from Kubernetes..."
	kubectl delete -f k8s/

k8s-logs: ## Show logs from Kubernetes pods
	@echo "Showing logs from microjobs pods..."
	kubectl logs -f -l app=api-gateway -n microjobs

k8s-status: ## Show Kubernetes deployment status
	@echo "Kubernetes deployment status:"
	kubectl get pods -n microjobs
	kubectl get services -n microjobs

# Monitoring Commands
monitor-start: ## Start monitoring stack
	@echo "Starting monitoring stack..."
	docker-compose up -d prometheus grafana jaeger
	@echo "Monitoring stack started!"
	@echo "Prometheus: http://localhost:9090"
	@echo "Grafana: http://localhost:3000 (admin/admin123)"
	@echo "Jaeger: http://localhost:16686"

monitor-stop: ## Stop monitoring stack
	@echo "Stopping monitoring stack..."
	docker-compose stop prometheus grafana jaeger

# Code Quality
lint: ## Run code quality checks
	@echo "Running code quality checks..."
	./gradlew checkstyleMain checkstyleTest
	./gradlew spotbugsMain spotbugsTest

format: ## Format code
	@echo "Formatting code..."
	./gradlew spotlessApply

# Security
security-scan: ## Run security scan
	@echo "Running security scan..."
	./gradlew dependencyCheckAnalyze

# Performance Testing
load-test: ## Run load tests
	@echo "Running load tests..."
	./gradlew :load-tests:gatlingRun

# Documentation
docs: ## Generate documentation
	@echo "Generating documentation..."
	./gradlew asciidoctor
	@echo "Documentation generated in build/docs/"

# Release Commands
release-prepare: ## Prepare for release
	@echo "Preparing for release..."
	./gradlew clean build test
	./gradlew dependencyCheckAnalyze
	@echo "Release preparation complete!"

release: ## Create release
	@echo "Creating release..."
	git tag -a v$(VERSION) -m "Release version $(VERSION)"
	git push origin v$(VERSION)
	@echo "Release v$(VERSION) created!"

# Utility Commands
logs: ## Show logs from running services
	@echo "Showing logs from running services..."
	docker-compose logs -f

ps: ## Show running containers
	@echo "Running containers:"
	docker-compose ps

restart-service: ## Restart specific service (usage: make restart-service SERVICE=jobs-service)
	@echo "Restarting $(SERVICE)..."
	docker-compose restart $(SERVICE)

# Health Checks
health-check: ## Check health of all services
	@echo "Checking health of all services..."
	@curl -s http://localhost:8080/actuator/health || echo "API Gateway: DOWN"
	@curl -s http://localhost:8083/actuator/health || echo "Jobs Service: DOWN"
	@curl -s http://localhost:8084/actuator/health || echo "Escrow Service: DOWN"

# Development Setup
setup: ## Initial setup for development
	@echo "Setting up development environment..."
	@if [ ! -f .env ]; then cp .env.example .env; fi
	@if [ ! -d ~/.gradle ]; then mkdir -p ~/.gradle; fi
	@echo "Development environment setup complete!"

# Backup Commands
backup-db: ## Backup database
	@echo "Backing up database..."
	docker exec postgres pg_dump -U microjobs microjobs > backup_$(shell date +%Y%m%d_%H%M%S).sql
	@echo "Database backup complete!"

restore-db: ## Restore database from backup (usage: make restore-db BACKUP=backup_file.sql)
	@echo "Restoring database from $(BACKUP)..."
	docker exec -i postgres psql -U microjobs microjobs < $(BACKUP)
	@echo "Database restore complete!"
