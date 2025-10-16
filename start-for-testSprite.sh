#!/bin/bash

# MicroJobs Marketplace - TestSprite Ready Setup
echo "🚀 Starting MicroJobs Marketplace for TestSprite Testing..."

# Check if infrastructure is running
echo "📡 Checking infrastructure services..."
docker-compose ps | grep -E "(postgres|redis|kafka|elasticsearch|keycloak)" | grep "Up"

# Start core services
echo "🔧 Starting core services..."

# Start Jobs Service
echo "Starting Jobs Service on port 8083..."
./gradlew :jobs-service:bootRun --no-daemon > logs/jobs-service.log 2>&1 &
JOBS_PID=$!

# Start Escrow Service  
echo "Starting Escrow Service on port 8084..."
./gradlew :escrow-service:bootRun --no-daemon > logs/escrow-service.log 2>&1 &
ESCROW_PID=$!

# Start API Gateway
echo "Starting API Gateway on port 8080..."
./gradlew :api-gateway:bootRun --no-daemon > logs/api-gateway.log 2>&1 &
GATEWAY_PID=$!

# Wait for services to start
echo "⏳ Waiting for services to start..."
sleep 30

# Test services
echo "🧪 Testing services..."

# Test Jobs Service
echo "Testing Jobs Service..."
curl -s http://localhost:8083/api/jobs/health && echo "✅ Jobs Service OK" || echo "❌ Jobs Service Failed"

# Test Escrow Service
echo "Testing Escrow Service..."
curl -s http://localhost:8084/api/escrow/health && echo "✅ Escrow Service OK" || echo "❌ Escrow Service Failed"

# Test API Gateway
echo "Testing API Gateway..."
curl -s http://localhost:8080/actuator/health && echo "✅ API Gateway OK" || echo "❌ API Gateway Failed"

# Test Demo Service
echo "Testing Demo Service..."
curl -s http://localhost:8086/health && echo "✅ Demo Service OK" || echo "❌ Demo Service Failed"

echo ""
echo "🎯 TestSprite Ready Endpoints:"
echo "• Demo Dashboard: http://localhost:8086/dashboard"
echo "• Jobs API: http://localhost:8083/api/jobs"
echo "• Escrow API: http://localhost:8084/api/escrow"
echo "• API Gateway: http://localhost:8080"
echo "• Keycloak Admin: http://localhost:8085/admin"
echo ""
echo "📊 Service Status:"
echo "• Jobs Service PID: $JOBS_PID"
echo "• Escrow Service PID: $ESCROW_PID"
echo "• API Gateway PID: $GATEWAY_PID"
echo ""
echo "🎉 MicroJobs Marketplace is ready for TestSprite testing!"
echo "📝 Logs available in logs/ directory"
