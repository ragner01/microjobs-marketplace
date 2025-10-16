# 🚀 Deployment Guide

## Overview

This guide covers deploying the MicroJobs Marketplace to various environments using Docker, Kubernetes, and cloud platforms.

## Prerequisites

- Docker 20.10+
- Docker Compose 2.0+
- Kubernetes 1.21+
- kubectl configured
- Helm 3.0+
- Java 11+
- Gradle 7.0+

## Local Development Deployment

### 1. Environment Setup

```bash
# Clone the repository
git clone https://github.com/ragner01/microjobs-marketplace.git
cd microjobs-marketplace

# Copy environment template
cp env.template .env

# Edit environment variables
nano .env
```

### 2. Start Infrastructure Services

```bash
# Start PostgreSQL, Redis, MinIO, Keycloak
docker-compose -f docker-compose.infra.yml up -d

# Wait for services to be ready
./scripts/wait-for-services.sh
```

### 3. Build and Start Services

```bash
# Build all services
./gradlew build -x test

# Start services individually
./gradlew :jobs-service:bootRun &
./gradlew :escrow-service:bootRun &
./gradlew :api-gateway:bootRun &
```

### 4. Verify Deployment

```bash
# Check service health
curl http://localhost:8083/actuator/health  # Jobs Service
curl http://localhost:8084/actuator/health  # Escrow Service
curl http://localhost:8080/actuator/health   # API Gateway
```

## Docker Deployment

### 1. Build Docker Images

```bash
# Build all service images
docker build -t microjobs-jobs-service:latest -f jobs-service/Dockerfile .
docker build -t microjobs-escrow-service:latest -f escrow-service/Dockerfile .
docker build -t microjobs-api-gateway:latest -f api-gateway/Dockerfile .
```

### 2. Docker Compose Deployment

```bash
# Start all services with Docker Compose
docker-compose up -d

# Check service status
docker-compose ps

# View logs
docker-compose logs -f jobs-service
```

### 3. Production Docker Compose

```bash
# Use production configuration
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

## Kubernetes Deployment

### 1. Create Namespace

```bash
kubectl create namespace microjobs
```

### 2. Deploy Infrastructure

```bash
# Deploy PostgreSQL
kubectl apply -f k8s/infrastructure/postgresql.yaml

# Deploy Redis
kubectl apply -f k8s/infrastructure/redis.yaml

# Deploy MinIO
kubectl apply -f k8s/infrastructure/minio.yaml

# Deploy Keycloak
kubectl apply -f k8s/infrastructure/keycloak.yaml
```

### 3. Deploy Application Services

```bash
# Deploy Jobs Service
kubectl apply -f k8s/services/jobs-service.yaml

# Deploy Escrow Service
kubectl apply -f k8s/services/escrow-service.yaml

# Deploy API Gateway
kubectl apply -f k8s/services/api-gateway.yaml
```

### 4. Deploy with Helm

```bash
# Add Helm repository
helm repo add microjobs https://charts.microjobs.com

# Install with Helm
helm install microjobs ./helm/microjobs \
  --namespace microjobs \
  --set image.tag=latest \
  --set ingress.enabled=true \
  --set ingress.host=api.microjobs.com
```

## Cloud Deployment

### AWS EKS Deployment

```bash
# Create EKS cluster
eksctl create cluster --name microjobs-cluster --region us-west-2

# Deploy with Helm
helm install microjobs ./helm/microjobs \
  --namespace microjobs \
  --set image.repository=your-account.dkr.ecr.us-west-2.amazonaws.com/microjobs \
  --set ingress.class=alb \
  --set ingress.annotations."kubernetes\.io/ingress\.class"=alb
```

### Google GKE Deployment

```bash
# Create GKE cluster
gcloud container clusters create microjobs-cluster \
  --zone us-central1-a \
  --num-nodes 3

# Deploy with Helm
helm install microjobs ./helm/microjobs \
  --namespace microjobs \
  --set image.repository=gcr.io/your-project/microjobs \
  --set ingress.class=gce
```

### Azure AKS Deployment

```bash
# Create AKS cluster
az aks create --resource-group microjobs-rg \
  --name microjobs-cluster \
  --node-count 3

# Deploy with Helm
helm install microjobs ./helm/microjobs \
  --namespace microjobs \
  --set image.repository=your-registry.azurecr.io/microjobs \
  --set ingress.class=azure
```

## Environment-Specific Configurations

### Development Environment

```yaml
# k8s/environments/dev.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: microjobs-config
data:
  SPRING_PROFILES_ACTIVE: dev
  LOG_LEVEL: DEBUG
  DB_URL: jdbc:postgresql://postgres-dev:5432/microjobs_dev
```

### Staging Environment

```yaml
# k8s/environments/staging.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: microjobs-config
data:
  SPRING_PROFILES_ACTIVE: staging
  LOG_LEVEL: INFO
  DB_URL: jdbc:postgresql://postgres-staging:5432/microjobs_staging
```

### Production Environment

```yaml
# k8s/environments/prod.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: microjobs-config
data:
  SPRING_PROFILES_ACTIVE: prod
  LOG_LEVEL: WARN
  DB_URL: jdbc:postgresql://postgres-prod:5432/microjobs_prod
```

## Database Migrations

### Flyway Migrations

```bash
# Run migrations in development
./gradlew flywayMigrate -Pflyway.url=jdbc:postgresql://localhost:5432/microjobs_dev

# Run migrations in staging
kubectl exec -it postgres-staging -- flyway migrate

# Run migrations in production
kubectl exec -it postgres-prod -- flyway migrate
```

### Manual Migration

```bash
# Connect to database
kubectl exec -it postgres-prod -- psql -U microjobs -d microjobs_prod

# Run migration scripts
\i /migrations/V1__Create_initial_schemas.sql
```

## Monitoring and Observability

### Prometheus Setup

```bash
# Deploy Prometheus
kubectl apply -f k8s/monitoring/prometheus.yaml

# Deploy Grafana
kubectl apply -f k8s/monitoring/grafana.yaml

# Access Grafana
kubectl port-forward svc/grafana 3000:80
```

### Jaeger Tracing

```bash
# Deploy Jaeger
kubectl apply -f k8s/monitoring/jaeger.yaml

# Access Jaeger UI
kubectl port-forward svc/jaeger-query 16686:80
```

### Log Aggregation

```bash
# Deploy ELK Stack
kubectl apply -f k8s/monitoring/elasticsearch.yaml
kubectl apply -f k8s/monitoring/kibana.yaml
kubectl apply -f k8s/monitoring/logstash.yaml
```

## Security Configuration

### SSL/TLS Setup

```bash
# Generate SSL certificates
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes

# Create Kubernetes secret
kubectl create secret tls microjobs-tls --key=key.pem --cert=cert.pem
```

### Network Policies

```bash
# Apply network policies
kubectl apply -f k8s/security/network-policies.yaml
```

### RBAC Configuration

```bash
# Apply RBAC rules
kubectl apply -f k8s/security/rbac.yaml
```

## Backup and Recovery

### Database Backup

```bash
# Create backup
kubectl exec postgres-prod -- pg_dump -U microjobs microjobs_prod > backup.sql

# Restore from backup
kubectl exec -i postgres-prod -- psql -U microjobs microjobs_prod < backup.sql
```

### Automated Backups

```bash
# Deploy backup cron job
kubectl apply -f k8s/backup/backup-cronjob.yaml
```

## Scaling

### Horizontal Pod Autoscaling

```bash
# Deploy HPA
kubectl apply -f k8s/scaling/hpa.yaml

# Check HPA status
kubectl get hpa
```

### Vertical Pod Autoscaling

```bash
# Deploy VPA
kubectl apply -f k8s/scaling/vpa.yaml
```

## Troubleshooting

### Common Issues

1. **Service Not Starting**
   ```bash
   # Check logs
   kubectl logs -f deployment/jobs-service
   
   # Check events
   kubectl get events --sort-by=.metadata.creationTimestamp
   ```

2. **Database Connection Issues**
   ```bash
   # Test database connectivity
   kubectl exec -it postgres-prod -- psql -U microjobs -d microjobs_prod -c "SELECT 1"
   ```

3. **Memory Issues**
   ```bash
   # Check resource usage
   kubectl top pods
   
   # Check memory limits
   kubectl describe pod jobs-service-xxx
   ```

### Debug Commands

```bash
# Get pod details
kubectl describe pod jobs-service-xxx

# Check service endpoints
kubectl get endpoints

# Port forward for debugging
kubectl port-forward svc/jobs-service 8083:8083
```

## Performance Optimization

### JVM Tuning

```yaml
# k8s/services/jobs-service.yaml
env:
- name: JAVA_OPTS
  value: "-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

### Database Optimization

```sql
-- Create indexes
CREATE INDEX CONCURRENTLY idx_jobs_tenant_status ON jobs.jobs(tenant_id, status);
CREATE INDEX CONCURRENTLY idx_escrow_accounts_holder ON escrow.escrow_accounts(account_holder_id);
```

## Rollback Procedures

### Application Rollback

```bash
# Rollback to previous version
kubectl rollout undo deployment/jobs-service

# Check rollback status
kubectl rollout status deployment/jobs-service
```

### Database Rollback

```bash
# Rollback database migration
kubectl exec postgres-prod -- flyway undo -target=1.0
```

## Maintenance

### Regular Maintenance Tasks

1. **Security Updates**
   ```bash
   # Update base images
   docker pull openjdk:11-jre-slim
   ```

2. **Dependency Updates**
   ```bash
   # Update Gradle dependencies
   ./gradlew dependencyUpdates
   ```

3. **Performance Monitoring**
   ```bash
   # Check metrics
   kubectl top pods
   kubectl top nodes
   ```

## Support

For deployment issues:
- **Documentation**: [GitHub Wiki](https://github.com/ragner01/microjobs-marketplace/wiki)
- **Issues**: [GitHub Issues](https://github.com/ragner01/microjobs-marketplace/issues)
- **Discussions**: [GitHub Discussions](https://github.com/ragner01/microjobs-marketplace/discussions)
