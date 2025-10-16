#!/bin/bash

# Keycloak Configuration Script for MicroJobs Marketplace
# This script configures Keycloak with the necessary realm, clients, and users

KEYCLOAK_URL="http://localhost:8085"
ADMIN_USER="admin"
ADMIN_PASSWORD="admin123"
REALM_NAME="microjobs"

echo "🔐 Configuring Keycloak for MicroJobs Marketplace..."

# Wait for Keycloak to be ready
echo "⏳ Waiting for Keycloak to be ready..."
until curl -s "$KEYCLOAK_URL/health/ready" > /dev/null; do
  echo "Waiting for Keycloak..."
  sleep 5
done

echo "✅ Keycloak is ready!"

# Get admin token
echo "🔑 Getting admin token..."
ADMIN_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASSWORD" \
  -d "grant_type=password" \
  -d "client_id=admin-cli" | jq -r '.access_token')

if [ "$ADMIN_TOKEN" = "null" ] || [ -z "$ADMIN_TOKEN" ]; then
  echo "❌ Failed to get admin token"
  exit 1
fi

echo "✅ Admin token obtained"

# Create realm
echo "🏰 Creating realm: $REALM_NAME"
curl -s -X POST "$KEYCLOAK_URL/admin/realms" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "realm": "'$REALM_NAME'",
    "enabled": true,
    "displayName": "MicroJobs Marketplace",
    "displayNameHtml": "<div class=\"kc-logo-text\"><span>MicroJobs</span></div>",
    "loginTheme": "keycloak",
    "accountTheme": "keycloak",
    "adminTheme": "keycloak",
    "emailTheme": "keycloak",
    "accessTokenLifespan": 300,
    "accessTokenLifespanForImplicitFlow": 900,
    "ssoSessionIdleTimeout": 1800,
    "ssoSessionMaxLifespan": 36000,
    "offlineSessionIdleTimeout": 2592000,
    "accessCodeLifespan": 60,
    "accessCodeLifespanUserAction": 300,
    "accessCodeLifespanLogin": 1800,
    "actionTokenGeneratedByAdminLifespan": 43200,
    "actionTokenGeneratedByUserLifespan": 300,
    "oauth2DeviceCodeLifespan": 600,
    "oauth2DevicePollingInterval": 5
  }'

echo "✅ Realm created"

# Create clients
echo "🔧 Creating clients..."

# API Gateway Client
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "api-gateway",
    "name": "API Gateway",
    "description": "MicroJobs API Gateway",
    "enabled": true,
    "clientAuthenticatorType": "client-secret",
    "secret": "api-gateway-secret",
    "redirectUris": ["http://localhost:8080/*"],
    "webOrigins": ["http://localhost:8080"],
    "protocol": "openid-connect",
    "publicClient": false,
    "serviceAccountsEnabled": true,
    "authorizationServicesEnabled": true,
    "standardFlowEnabled": true,
    "implicitFlowEnabled": false,
    "directAccessGrantsEnabled": true,
    "attributes": {
      "access.token.lifespan": "300",
      "client.session.idle.timeout": "1800",
      "client.session.max.lifespan": "36000"
    }
  }'

# Admin Console Client
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "admin-console",
    "name": "Admin Console",
    "description": "MicroJobs Admin Console",
    "enabled": true,
    "clientAuthenticatorType": "client-secret",
    "secret": "admin-console-secret",
    "redirectUris": ["http://localhost:3000/*"],
    "webOrigins": ["http://localhost:3000"],
    "protocol": "openid-connect",
    "publicClient": false,
    "serviceAccountsEnabled": true,
    "authorizationServicesEnabled": true,
    "standardFlowEnabled": true,
    "implicitFlowEnabled": false,
    "directAccessGrantsEnabled": true
  }'

# Mobile App Client (Public)
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/clients" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": "mobile-app",
    "name": "Mobile App",
    "description": "MicroJobs Mobile Application",
    "enabled": true,
    "protocol": "openid-connect",
    "publicClient": true,
    "serviceAccountsEnabled": false,
    "authorizationServicesEnabled": false,
    "standardFlowEnabled": true,
    "implicitFlowEnabled": false,
    "directAccessGrantsEnabled": true,
    "redirectUris": ["com.microjobs://callback"],
    "webOrigins": ["*"]
  }'

echo "✅ Clients created"

# Create roles
echo "👥 Creating roles..."

# Client Roles
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "client",
    "description": "Job client role"
  }'

curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "worker",
    "description": "Job worker role"
  }'

curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "admin",
    "description": "System administrator role"
  }'

curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "tenant-admin",
    "description": "Tenant administrator role"
  }'

echo "✅ Roles created"

# Create users
echo "👤 Creating users..."

# Admin User
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@microjobs.com",
    "firstName": "System",
    "lastName": "Administrator",
    "enabled": true,
    "emailVerified": true,
    "credentials": [{
      "type": "password",
      "value": "admin123",
      "temporary": false
    }]
  }'

# Get admin user ID
ADMIN_USER_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users?username=admin" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')

# Assign admin role to admin user
ADMIN_ROLE_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles/admin" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.id')

curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$ADMIN_USER_ID/role-mappings/realm" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "[{\"id\":\"$ADMIN_ROLE_ID\",\"name\":\"admin\"}]"

# Demo Client User
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo-client",
    "email": "client@demo.com",
    "firstName": "Demo",
    "lastName": "Client",
    "enabled": true,
    "emailVerified": true,
    "credentials": [{
      "type": "password",
      "value": "password123",
      "temporary": false
    }]
  }'

# Get demo client user ID
CLIENT_USER_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users?username=demo-client" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')

# Assign client role to demo client user
CLIENT_ROLE_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles/client" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.id')

curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$CLIENT_USER_ID/role-mappings/realm" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "[{\"id\":\"$CLIENT_ROLE_ID\",\"name\":\"client\"}]"

# Demo Worker User
curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo-worker",
    "email": "worker@demo.com",
    "firstName": "Demo",
    "lastName": "Worker",
    "enabled": true,
    "emailVerified": true,
    "credentials": [{
      "type": "password",
      "value": "password123",
      "temporary": false
    }]
  }'

# Get demo worker user ID
WORKER_USER_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users?username=demo-worker" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.[0].id')

# Assign worker role to demo worker user
WORKER_ROLE_ID=$(curl -s -X GET "$KEYCLOAK_URL/admin/realms/$REALM_NAME/roles/worker" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq -r '.id')

curl -s -X POST "$KEYCLOAK_URL/admin/realms/$REALM_NAME/users/$WORKER_USER_ID/role-mappings/realm" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "[{\"id\":\"$WORKER_ROLE_ID\",\"name\":\"worker\"}]"

echo "✅ Users created"

# Configure realm settings
echo "⚙️ Configuring realm settings..."

# Enable registration
curl -s -X PUT "$KEYCLOAK_URL/admin/realms/$REALM_NAME" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "registrationAllowed": true,
    "registrationEmailAsUsername": true,
    "rememberMe": true,
    "verifyEmail": true,
    "loginWithEmailAllowed": true,
    "duplicateEmailsAllowed": false,
    "resetPasswordAllowed": true,
    "editUsernameAllowed": false,
    "bruteForceProtected": true
  }'

echo "✅ Realm settings configured"

echo ""
echo "🎉 Keycloak configuration completed!"
echo ""
echo "📋 Configuration Summary:"
echo "========================"
echo "Realm: $REALM_NAME"
echo "Admin Console: $KEYCLOAK_URL/admin"
echo "Admin User: admin / admin123"
echo ""
echo "👥 Demo Users:"
echo "- Admin: admin@microjobs.com / admin123"
echo "- Client: client@demo.com / password123"
echo "- Worker: worker@demo.com / password123"
echo ""
echo "🔧 Clients:"
echo "- API Gateway: api-gateway (secret: api-gateway-secret)"
echo "- Admin Console: admin-console (secret: admin-console-secret)"
echo "- Mobile App: mobile-app (public client)"
echo ""
echo "👤 Roles:"
echo "- admin: System administrator"
echo "- client: Job client"
echo "- worker: Job worker"
echo "- tenant-admin: Tenant administrator"
echo ""
echo "🔗 Keycloak Admin Console: $KEYCLOAK_URL/admin"
echo "🔗 MicroJobs Admin Console: http://localhost:3000"
echo "🔗 API Gateway: http://localhost:8080"
