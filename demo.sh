#!/bin/bash

# MicroJobs Marketplace - End-to-End Demo Script
# This script demonstrates the complete workflow of the micro-jobs marketplace

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
API_BASE_URL="http://localhost:8080"
JOBS_SERVICE_URL="http://localhost:8083"
ESCROW_SERVICE_URL="http://localhost:8084"
AUTH_SERVICE_URL="http://localhost:8081"

# Demo data
TENANT_ID="demo-tenant-001"
CLIENT_ID="client-001"
WORKER_ID="worker-001"
JOB_TITLE="Website Development"
JOB_DESCRIPTION="Need a professional website for my business"
JOB_BUDGET="50000"
JOB_CURRENCY="NGN"

echo -e "${BLUE}🚀 MicroJobs Marketplace - End-to-End Demo${NC}"
echo "=================================================="
echo ""

# Function to print step headers
print_step() {
    echo -e "${YELLOW}📋 Step $1: $2${NC}"
    echo "----------------------------------------"
}

# Function to make API calls
api_call() {
    local method=$1
    local url=$2
    local data=$3
    local headers=$4
    
    if [ -n "$data" ]; then
        curl -s -X $method "$url" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $DEMO_TOKEN" \
            -H "$headers" \
            -d "$data"
    else
        curl -s -X $method "$url" \
            -H "Authorization: Bearer $DEMO_TOKEN" \
            -H "$headers"
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local service_url=$2
    local max_attempts=30
    local attempt=1
    
    echo "Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$service_url/actuator/health" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name is ready!${NC}"
            return 0
        fi
        
        echo "Attempt $attempt/$max_attempts - $service_name not ready yet..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}❌ $service_name failed to start within timeout${NC}"
    return 1
}

# Function to check if services are running
check_services() {
    print_step "0" "Checking Service Health"
    
    wait_for_service "API Gateway" "$API_BASE_URL"
    wait_for_service "Jobs Service" "$JOBS_SERVICE_URL"
    wait_for_service "Escrow Service" "$ESCROW_SERVICE_URL"
    wait_for_service "Auth Service" "$AUTH_SERVICE_URL"
    
    echo ""
}

# Function to create tenant
create_tenant() {
    print_step "1" "Creating Demo Tenant"
    
    local tenant_data='{
        "name": "Demo Company",
        "domain": "demo-company.com",
        "settings": {
            "currency": "NGN",
            "timezone": "Africa/Lagos",
            "features": ["escrow", "disputes", "analytics"]
        }
    }'
    
    local response=$(api_call "POST" "$API_BASE_URL/api/tenants" "$tenant_data")
    echo "Tenant created: $response"
    echo ""
}

# Function to authenticate client
authenticate_client() {
    print_step "2" "Authenticating Client"
    
    local auth_data='{
        "email": "client@demo-company.com",
        "password": "client123",
        "tenantId": "'$TENANT_ID'"
    }'
    
    local response=$(api_call "POST" "$AUTH_SERVICE_URL/api/auth/login" "$auth_data")
    DEMO_TOKEN=$(echo $response | jq -r '.token')
    
    if [ "$DEMO_TOKEN" = "null" ] || [ -z "$DEMO_TOKEN" ]; then
        echo -e "${RED}❌ Authentication failed${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Client authenticated successfully${NC}"
    echo "Token: ${DEMO_TOKEN:0:20}..."
    echo ""
}

# Function to create escrow accounts
create_escrow_accounts() {
    print_step "3" "Creating Escrow Accounts"
    
    # Create client account
    local client_account_data='{
        "accountHolderId": "'$CLIENT_ID'",
        "accountType": "CLIENT",
        "initialBalance": {
            "amount": 100000,
            "currency": "NGN"
        }
    }'
    
    local client_response=$(api_call "POST" "$ESCROW_SERVICE_URL/api/escrow/accounts" "$client_account_data")
    echo "Client account created: $client_response"
    
    # Create worker account
    local worker_account_data='{
        "accountHolderId": "'$WORKER_ID'",
        "accountType": "WORKER",
        "initialBalance": {
            "amount": 0,
            "currency": "NGN"
        }
    }'
    
    local worker_response=$(api_call "POST" "$ESCROW_SERVICE_URL/api/escrow/accounts" "$worker_account_data")
    echo "Worker account created: $worker_response"
    echo ""
}

# Function to post a job
post_job() {
    print_step "4" "Posting a Job"
    
    local job_data='{
        "title": "'$JOB_TITLE'",
        "description": "'$JOB_DESCRIPTION'",
        "budget": {
            "amount": '$JOB_BUDGET',
            "currency": "'$JOB_CURRENCY'"
        },
        "deadline": "2024-02-15T23:59:59",
        "requiredSkills": ["React", "Node.js", "MongoDB"],
        "location": "Lagos, Nigeria",
        "latitude": 6.5244,
        "longitude": 3.3792,
        "maxDistanceKm": 50
    }'
    
    local response=$(api_call "POST" "$JOBS_SERVICE_URL/api/jobs" "$job_data")
    if ! echo "$response" | jq -e . > /dev/null 2>&1; then
        echo -e "${RED}❌ Job creation failed. Invalid JSON response:${NC}"
        echo "$response"
        exit 1
    fi
    JOB_ID=$(echo $response | jq -r '.id')
    
    if [ "$JOB_ID" = "null" ] || [ -z "$JOB_ID" ]; then
        echo -e "${RED}❌ Job creation failed${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Job posted successfully${NC}"
    echo "Job ID: $JOB_ID"
    echo "Job Details: $response"
    echo ""
}

# Function to submit a bid
submit_bid() {
    print_step "5" "Submitting a Bid"
    
    local bid_data='{
        "jobId": "'$JOB_ID'",
        "bidAmount": {
            "amount": 45000,
            "currency": "'$JOB_CURRENCY'"
        },
        "proposal": "I will create a modern, responsive website using React and Node.js. I have 5 years of experience in web development.",
        "estimatedCompletionDays": 14
    }'
    
    local response=$(api_call "POST" "$JOBS_SERVICE_URL/api/jobs/$JOB_ID/bids" "$bid_data")
    BID_ID=$(echo $response | jq -r '.id')
    
    if [ "$BID_ID" = "null" ] || [ -z "$BID_ID" ]; then
        echo -e "${RED}❌ Bid submission failed${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Bid submitted successfully${NC}"
    echo "Bid ID: $BID_ID"
    echo "Bid Details: $response"
    echo ""
}

# Function to accept a bid
accept_bid() {
    print_step "6" "Accepting the Bid"
    
    local response=$(api_call "PUT" "$JOBS_SERVICE_URL/api/jobs/$JOB_ID/bids/$BID_ID/accept" "")
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Bid accepted successfully${NC}"
        echo "Job assigned to worker: $WORKER_ID"
    else
        echo -e "${RED}❌ Bid acceptance failed${NC}"
        exit 1
    fi
    echo ""
}

# Function to initiate escrow transaction
initiate_escrow() {
    print_step "7" "Initiating Escrow Transaction"
    
    local escrow_data='{
        "jobId": "'$JOB_ID'",
        "clientId": "'$CLIENT_ID'",
        "workerId": "'$WORKER_ID'",
        "amount": {
            "amount": 45000,
            "currency": "'$JOB_CURRENCY'"
        },
        "description": "Payment for job: '$JOB_TITLE'"
    }'
    
    local response=$(api_call "POST" "$ESCROW_SERVICE_URL/api/escrow/transactions" "$escrow_data")
    TRANSACTION_ID=$(echo $response | jq -r '.id')
    
    if [ "$TRANSACTION_ID" = "null" ] || [ -z "$TRANSACTION_ID" ]; then
        echo -e "${RED}❌ Escrow transaction initiation failed${NC}"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Escrow transaction initiated${NC}"
    echo "Transaction ID: $TRANSACTION_ID"
    echo "Transaction Details: $response"
    echo ""
}

# Function to check escrow status
check_escrow_status() {
    print_step "8" "Checking Escrow Status"
    
    local response=$(api_call "GET" "$ESCROW_SERVICE_URL/api/escrow/transactions/$TRANSACTION_ID" "")
    local status=$(echo $response | jq -r '.status')
    
    echo "Escrow Status: $status"
    echo "Transaction Details: $response"
    echo ""
}

# Function to simulate job completion
complete_job() {
    print_step "9" "Marking Job as Completed"
    
    local response=$(api_call "PUT" "$JOBS_SERVICE_URL/api/jobs/$JOB_ID/complete" "")
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Job marked as completed${NC}"
    else
        echo -e "${RED}❌ Job completion failed${NC}"
        exit 1
    fi
    echo ""
}

# Function to release payment
release_payment() {
    print_step "10" "Releasing Payment to Worker"
    
    local response=$(api_call "POST" "$ESCROW_SERVICE_URL/api/escrow/transactions/$TRANSACTION_ID/release" "")
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Payment released to worker${NC}"
    else
        echo -e "${RED}❌ Payment release failed${NC}"
        exit 1
    fi
    echo ""
}

# Function to check final balances
check_final_balances() {
    print_step "11" "Checking Final Account Balances"
    
    echo "Client Account Balance:"
    local client_balance=$(api_call "GET" "$ESCROW_SERVICE_URL/api/escrow/accounts/client/$CLIENT_ID" "")
    echo "$client_balance"
    echo ""
    
    echo "Worker Account Balance:"
    local worker_balance=$(api_call "GET" "$ESCROW_SERVICE_URL/api/escrow/accounts/worker/$WORKER_ID" "")
    echo "$worker_balance"
    echo ""
}

# Function to demonstrate dispute flow
demonstrate_dispute() {
    print_step "12" "Demonstrating Dispute Flow"
    
    echo "Creating a dispute for demonstration..."
    local dispute_data='{
        "jobId": "'$JOB_ID'",
        "transactionId": "'$TRANSACTION_ID'",
        "reason": "Work quality not as expected",
        "description": "The delivered work does not meet the requirements specified in the job description."
    }'
    
    local response=$(api_call "POST" "$API_BASE_URL/api/disputes" "$dispute_data")
    DISPUTE_ID=$(echo $response | jq -r '.id')
    
    if [ "$DISPUTE_ID" != "null" ] && [ -n "$DISPUTE_ID" ]; then
        echo -e "${GREEN}✅ Dispute created successfully${NC}"
        echo "Dispute ID: $DISPUTE_ID"
        echo "Dispute Details: $response"
    else
        echo -e "${YELLOW}⚠️ Dispute creation failed (service may not be running)${NC}"
    fi
    echo ""
}

# Function to show analytics
show_analytics() {
    print_step "13" "Showing Analytics Dashboard"
    
    echo "Job Statistics:"
    local job_stats=$(api_call "GET" "$JOBS_SERVICE_URL/api/jobs/stats" "")
    echo "$job_stats"
    echo ""
    
    echo "Escrow Statistics:"
    local escrow_stats=$(api_call "GET" "$ESCROW_SERVICE_URL/api/escrow/stats" "")
    echo "$escrow_stats"
    echo ""
}

# Function to cleanup demo data
cleanup_demo() {
    print_step "14" "Cleaning Up Demo Data"
    
    echo "This step would clean up the demo data in a real scenario."
    echo "For this demo, we'll leave the data for inspection."
    echo ""
}

# Function to show monitoring
show_monitoring() {
    print_step "15" "Monitoring & Observability"
    
    echo "Service Health Status:"
    echo "====================="
    
    echo "API Gateway Health:"
    curl -s "$API_BASE_URL/actuator/health" | jq '.'
    echo ""
    
    echo "Jobs Service Health:"
    curl -s "$JOBS_SERVICE_URL/actuator/health" | jq '.'
    echo ""
    
    echo "Escrow Service Health:"
    curl -s "$ESCROW_SERVICE_URL/actuator/health" | jq '.'
    echo ""
    
    echo "Metrics Endpoints:"
    echo "- Prometheus: http://localhost:9090"
    echo "- Grafana: http://localhost:3000"
    echo "- Jaeger: http://localhost:16686"
    echo ""
}

# Main execution
main() {
    echo "Starting MicroJobs Marketplace Demo..."
    echo "====================================="
    echo ""
    
    # Check if jq is installed
    if ! command -v jq &> /dev/null; then
        echo -e "${RED}❌ jq is required but not installed. Please install jq first.${NC}"
        exit 1
    fi
    
    # Check if services are running
    check_services
    
    # Execute demo steps
    create_tenant
    authenticate_client
    create_escrow_accounts
    post_job
    submit_bid
    accept_bid
    initiate_escrow
    check_escrow_status
    complete_job
    release_payment
    check_final_balances
    demonstrate_dispute
    show_analytics
    cleanup_demo
    show_monitoring
    
    echo -e "${GREEN}🎉 Demo completed successfully!${NC}"
    echo ""
    echo "Summary:"
    echo "========"
    echo "✅ Tenant created and authenticated"
    echo "✅ Escrow accounts created"
    echo "✅ Job posted and bid submitted"
    echo "✅ Bid accepted and escrow initiated"
    echo "✅ Job completed and payment released"
    echo "✅ Dispute flow demonstrated"
    echo "✅ Analytics and monitoring shown"
    echo ""
    echo "You can now explore the system through:"
    echo "- API Gateway: $API_BASE_URL"
    echo "- Grafana Dashboard: http://localhost:3000"
    echo "- Kibana: http://localhost:5601"
    echo "- Jaeger Tracing: http://localhost:16686"
}

# Run the demo
main "$@"
