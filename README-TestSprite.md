# 🎯 MicroJobs Marketplace - TestSprite Ready

## 🚀 Quick Start for TestSprite Testing

### Prerequisites
- Docker and Docker Compose
- Java 11
- Gradle

### 1. Start Infrastructure
```bash
make local-infra
```

### 2. Start Services for Testing
```bash
./start-for-testSprite.sh
```

### 3. Verify Services
```bash
# Demo Dashboard
curl http://localhost:8086/dashboard

# Jobs Service Health
curl http://localhost:8083/api/jobs/health

# Escrow Service Health  
curl http://localhost:8084/api/escrow/health

# API Gateway Health
curl http://localhost:8080/actuator/health
```

## 📊 Available Endpoints

### Jobs Service (Port 8083)
- `POST /api/jobs` - Create job
- `GET /api/jobs/{id}` - Get job
- `GET /api/jobs` - List jobs
- `POST /api/jobs/{jobId}/bids` - Submit bid
- `GET /api/jobs/{jobId}/bids` - Get bids
- `PUT /api/jobs/{jobId}/assign/{bidId}` - Assign job
- `PUT /api/jobs/{jobId}/complete` - Complete job

### Escrow Service (Port 8084)
- `POST /api/escrow/accounts` - Create account
- `GET /api/escrow/accounts/{id}` - Get account
- `GET /api/escrow/accounts` - List accounts
- `POST /api/escrow/accounts/{accountId}/deposit` - Deposit
- `POST /api/escrow/accounts/{accountId}/withdraw` - Withdraw
- `POST /api/escrow/transactions` - Create transaction
- `GET /api/escrow/transactions/{id}` - Get transaction
- `GET /api/escrow/transactions` - List transactions
- `PUT /api/escrow/transactions/{id}/complete` - Complete transaction

### Demo Service (Port 8086)
- `GET /dashboard` - System dashboard
- `GET /health` - Health check
- `GET /status` - System status
- `GET /services` - Service information

## 🧪 TestSprite Configuration

The project includes a comprehensive TestSprite configuration file:
- `testSprite-config.json` - Complete API specification
- Sample data and test scenarios
- Multi-tenant test cases
- Complete job lifecycle testing

## 🏗️ Architecture

- **Domain-Driven Design (DDD)**
- **Hexagonal Architecture**
- **Event Sourcing with Kafka**
- **Multi-tenant PostgreSQL**
- **Spring Boot 2.7.18**
- **Java 11**

## 📈 Monitoring

- **Demo Dashboard**: http://localhost:8086/dashboard
- **Keycloak Admin**: http://localhost:8085/admin
- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601
- **MinIO Console**: http://localhost:9001

## 🔧 Troubleshooting

### Services Not Starting
```bash
# Check logs
tail -f logs/jobs-service.log
tail -f logs/escrow-service.log
tail -f logs/api-gateway.log

# Restart services
pkill -f "gradle.*bootRun"
./start-for-testSprite.sh
```

### Database Issues
```bash
# Check PostgreSQL
docker exec -it springbootproject-postgres-1 psql -U microjobs -d microjobs

# Verify schemas
\dn
```

### Port Conflicts
```bash
# Check what's using ports
netstat -an | grep LISTEN | grep -E "808[0-9]"

# Kill conflicting processes
lsof -ti:8080 | xargs kill
lsof -ti:8083 | xargs kill
lsof -ti:8084 | xargs kill
```

## 🎉 Ready for TestSprite!

The MicroJobs Marketplace is now fully configured and ready for comprehensive testing with TestSprite. All APIs are functional, the database is properly set up, and the infrastructure is running.

**Happy Testing!** 🚀
