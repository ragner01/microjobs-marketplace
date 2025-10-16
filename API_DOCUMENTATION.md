# 📚 API Documentation

## Overview

The MicroJobs Marketplace provides a comprehensive REST API for managing micro-jobs, escrow transactions, and multi-tenant operations.

## Base URLs

- **Development**: `http://localhost:8080` (API Gateway)
- **Jobs Service**: `http://localhost:8083`
- **Escrow Service**: `http://localhost:8084`
- **Auth Service**: `http://localhost:8081`
- **Tenant Service**: `http://localhost:8082`

## Authentication

All API endpoints require authentication using JWT tokens obtained from Keycloak.

```bash
# Get access token
curl -X POST http://localhost:8085/realms/microjobs/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=microjobs-backend&client_secret=YOUR_CLIENT_SECRET"

# Use token in requests
curl -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  http://localhost:8083/api/jobs
```

## Jobs Service API

### Create Job
```http
POST /api/jobs
Content-Type: application/json

{
  "tenantId": "tenant-001",
  "title": "Website Design",
  "description": "Create a modern website for our business",
  "budgetAmount": 500.00,
  "budgetCurrency": "USD",
  "deadline": "2024-12-31T23:59:59",
  "requiredSkills": ["HTML", "CSS", "JavaScript"],
  "location": "New York, NY",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "maxDistanceKm": 50,
  "clientId": "client-001"
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "OPEN",
  "title": "Website Design",
  "description": "Create a modern website for our business",
  "budget": {
    "amount": 500.00,
    "currency": "USD"
  },
  "deadline": "2024-12-31T23:59:59",
  "requiredSkills": ["HTML", "CSS", "JavaScript"],
  "location": "New York, NY",
  "latitude": 40.7128,
  "longitude": -74.0060,
  "maxDistanceKm": 50.0,
  "clientId": "client-001",
  "tenantId": "tenant-001",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Get Job
```http
GET /api/jobs/{jobId}
```

### List Jobs
```http
GET /api/jobs?tenantId=tenant-001&status=OPEN&clientId=client-001
```

### Submit Bid
```http
POST /api/jobs/{jobId}/bids
Content-Type: application/json

{
  "workerId": "worker-001",
  "bidAmount": 450.00,
  "bidCurrency": "USD",
  "proposal": "I can create a modern, responsive website using React and Node.js",
  "estimatedCompletionDays": 14
}
```

### Assign Job
```http
PUT /api/jobs/{jobId}/assign/{bidId}
```

### Complete Job
```http
PUT /api/jobs/{jobId}/complete
```

## Escrow Service API

### Create Escrow Account
```http
POST /api/escrow/accounts
Content-Type: application/json

{
  "tenantId": "tenant-001",
  "accountHolderId": "client-001",
  "accountType": "CLIENT"
}
```

**Response:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "accountHolderId": "client-001",
  "accountType": "CLIENT",
  "balance": {
    "amount": 0.00,
    "currency": "USD"
  },
  "status": "ACTIVE",
  "tenantId": "tenant-001",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00"
}
```

### Deposit Funds
```http
POST /api/escrow/accounts/{accountId}/deposit
Content-Type: application/json

{
  "amount": 500.00,
  "currency": "USD",
  "description": "Initial deposit for job escrow"
}
```

### Withdraw Funds
```http
POST /api/escrow/accounts/{accountId}/withdraw
Content-Type: application/json

{
  "amount": 100.00,
  "currency": "USD",
  "description": "Partial withdrawal"
}
```

### Create Escrow Transaction
```http
POST /api/escrow/transactions
Content-Type: application/json

{
  "tenantId": "tenant-001",
  "jobId": "550e8400-e29b-41d4-a716-446655440000",
  "clientId": "client-001",
  "workerId": "worker-001",
  "amount": 500.00,
  "currency": "USD",
  "type": "JOB_ESCROW",
  "description": "Escrow for website design job"
}
```

### Complete Transaction
```http
PUT /api/escrow/transactions/{transactionId}/complete
```

## Error Responses

All endpoints return consistent error responses:

```json
{
  "timestamp": "2024-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/jobs",
  "details": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ]
}
```

## Rate Limiting

- **Rate Limit**: 1000 requests per hour per API key
- **Headers**: 
  - `X-RateLimit-Limit`: Request limit
  - `X-RateLimit-Remaining`: Remaining requests
  - `X-RateLimit-Reset`: Reset time (Unix timestamp)

## Pagination

List endpoints support pagination:

```http
GET /api/jobs?page=0&size=20&sort=createdAt,desc
```

**Response Headers:**
- `X-Total-Count`: Total number of items
- `X-Total-Pages`: Total number of pages

## Filtering and Search

### Jobs Filtering
```http
GET /api/jobs?status=OPEN&minBudget=100&maxBudget=1000&location=New York
```

### Escrow Transactions Filtering
```http
GET /api/escrow/transactions?status=COMPLETED&type=JOB_ESCROW&fromDate=2024-01-01
```

## Webhooks

The system supports webhooks for real-time notifications:

### Job Events
- `job.created`
- `job.assigned`
- `job.completed`
- `job.cancelled`

### Escrow Events
- `escrow.transaction.initiated`
- `escrow.transaction.completed`
- `escrow.transaction.failed`

### Webhook Payload Example
```json
{
  "event": "job.created",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "jobId": "550e8400-e29b-41d4-a716-446655440000",
    "tenantId": "tenant-001",
    "title": "Website Design",
    "budget": 500.00,
    "currency": "USD"
  }
}
```

## SDK Examples

### Java
```java
// Using Spring WebClient
WebClient client = WebClient.builder()
    .baseUrl("http://localhost:8083")
    .defaultHeader("Authorization", "Bearer " + accessToken)
    .build();

Job job = client.post()
    .uri("/api/jobs")
    .bodyValue(createJobRequest)
    .retrieve()
    .bodyToMono(Job.class)
    .block();
```

### JavaScript/Node.js
```javascript
const axios = require('axios');

const client = axios.create({
  baseURL: 'http://localhost:8083',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  }
});

const job = await client.post('/api/jobs', {
  tenantId: 'tenant-001',
  title: 'Website Design',
  description: 'Create a modern website',
  budgetAmount: 500.00,
  budgetCurrency: 'USD',
  clientId: 'client-001'
});
```

### Python
```python
import requests

headers = {
    'Authorization': f'Bearer {access_token}',
    'Content-Type': 'application/json'
}

response = requests.post(
    'http://localhost:8083/api/jobs',
    json={
        'tenantId': 'tenant-001',
        'title': 'Website Design',
        'description': 'Create a modern website',
        'budgetAmount': 500.00,
        'budgetCurrency': 'USD',
        'clientId': 'client-001'
    },
    headers=headers
)

job = response.json()
```

## Testing

### Postman Collection
Import the provided Postman collection for easy API testing:
- `postman/MicroJobs-API.postman_collection.json`

### Test Data
Use the provided test data for development:
- `test-data/sample-jobs.json`
- `test-data/sample-accounts.json`

## Support

For API support and questions:
- **Documentation**: [GitHub Wiki](https://github.com/ragner01/microjobs-marketplace/wiki)
- **Issues**: [GitHub Issues](https://github.com/ragner01/microjobs-marketplace/issues)
- **Discussions**: [GitHub Discussions](https://github.com/ragner01/microjobs-marketplace/discussions)
