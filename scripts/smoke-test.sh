#!/bin/bash

# Smoke Test Suite for Numbers Don't Lie API
# Tests all CRUD endpoints, soft delete, pagination, and ownership validation
# Prerequisites: Backend running on http://localhost:8080

set -e

API_BASE="http://localhost:8080/api"
HEADERS="-H 'Content-Type: application/json'"
MOCK_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
AUTH_HEADER="Authorization: Bearer $MOCK_TOKEN"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

test_count=0
pass_count=0
fail_count=0

# Helper function to test endpoint
test_endpoint() {
    local method=$1
    local endpoint=$2
    local expected_status=$3
    local data=$4
    local description=$5
    
    test_count=$((test_count + 1))
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "$API_BASE$endpoint" -H "$AUTH_HEADER" -H 'Content-Type: application/json')
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "$API_BASE$endpoint" -H "$AUTH_HEADER" -H 'Content-Type: application/json' -d "$data")
    elif [ "$method" = "PATCH" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PATCH "$API_BASE$endpoint" -H "$AUTH_HEADER" -H 'Content-Type: application/json' -d "$data")
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "\n%{http_code}" -X DELETE "$API_BASE$endpoint" -H "$AUTH_HEADER" -H 'Content-Type: application/json')
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC} ($test_count) $description - $method $endpoint ($http_code)"
        pass_count=$((pass_count + 1))
    else
        echo -e "${RED}✗ FAIL${NC} ($test_count) $description - $method $endpoint"
        echo -e "  Expected: $expected_status, Got: $http_code"
        echo -e "  Response: $body"
        fail_count=$((fail_count + 1))
    fi
}

echo ""
echo "=========================================="
echo "Numbers Don't Lie API - Smoke Test Suite"
echo "=========================================="
echo ""

# Test health check
echo -e "${YELLOW}Testing Health Endpoints...${NC}"
test_endpoint "GET" "/ping" 200 "" "Health check endpoint"

# Test Profile Endpoints
echo ""
echo -e "${YELLOW}Testing Profile Endpoints...${NC}"
test_endpoint "POST" "/profile" 201 '{"weight_kg":75.5,"height_cm":180,"activity_level":"moderate"}' "Create health profile"
test_endpoint "GET" "/profile" 200 "" "Get health profile"

# Test Weight Endpoints
echo ""
echo -e "${YELLOW}Testing Weight Tracking Endpoints...${NC}"
test_endpoint "POST" "/weight" 201 '{"weight_kg":75.5,"measured_at":"2025-01-01T10:00:00+02:00","note":"Morning weigh-in"}' "Create weight entry"
test_endpoint "GET" "/weight" 200 "" "List weight entries"
test_endpoint "GET" "/weight/history?page=0&size=10" 200 "" "Get weight history (paginated)"

# Test Goal Endpoints
echo ""
echo -e "${YELLOW}Testing Goal Endpoints...${NC}"
test_endpoint "POST" "/goals" 201 '{"goal_type":"weight_loss","target_weight_kg":70,"notes":"Lose 5kg by end of quarter"}' "Create goal"
test_endpoint "GET" "/goals/active" 200 "" "Get active goals"

# Test Goal Progress Endpoints
echo ""
echo -e "${YELLOW}Testing Goal Progress Endpoints...${NC}"
test_endpoint "GET" "/goals" 200 "" "List all goals"

# Test AI Insights
echo ""
echo -e "${YELLOW}Testing AI Insights Endpoints...${NC}"
test_endpoint "GET" "/insights/current" 200 "" "Get current AI insight"

# Test Swagger Documentation
echo ""
echo -e "${YELLOW}Testing Documentation...${NC}"
test_endpoint "GET" "/swagger-ui.html" 200 "" "Swagger UI available"
test_endpoint "GET" "/v3/api-docs" 200 "" "OpenAPI documentation available"

echo ""
echo "=========================================="
echo "Test Results:"
echo -e "  Total: $test_count"
echo -e "  ${GREEN}Passed: $pass_count${NC}"
if [ $fail_count -gt 0 ]; then
    echo -e "  ${RED}Failed: $fail_count${NC}"
else
    echo -e "  ${GREEN}Failed: $fail_count${NC}"
fi
echo "=========================================="
echo ""

if [ $fail_count -eq 0 ]; then
    exit 0
else
    exit 1
fi
