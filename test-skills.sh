#!/bin/bash

# Skill Testing Script
# Tests all available skills with example commands

set -e

API_URL="http://localhost:8080/api/agent"
BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     Spring AI Agent - Skill Tester        ║${NC}"
echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
echo ""

# Check if server is running
if ! curl -s "$API_URL/skills" > /dev/null 2>&1; then
    echo -e "${YELLOW}⚠️  Server not running on port 8080${NC}"
    echo "Please start the server first: mvn spring-boot:run"
    exit 1
fi

# Function to test a skill
test_skill() {
    local skill_name=$1
    local goal=$2
    
    echo -e "${GREEN}Testing ${skill_name}...${NC}"
    echo -e "${YELLOW}Goal:${NC} \"$goal\""
    
    response=$(curl -s -X POST "$API_URL/execute" \
        -H "Content-Type: application/json" \
        -d "{\"goal\": \"$goal\"}")
    
    # Extract key information
    skill_used=$(echo "$response" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data['trace'][0]['skillName'])" 2>/dev/null || echo "Error")
    output=$(echo "$response" | python3 -c "import sys, json; data=json.load(sys.stdin); print(data['finalOutput'][:100])" 2>/dev/null || echo "Error parsing response")
    
    echo -e "${GREEN}Skill Used:${NC} $skill_used"
    echo -e "${GREEN}Output:${NC} $output"
    echo ""
}

# Display available skills
echo -e "${BLUE}📋 Available Skills:${NC}"
skills=$(curl -s "$API_URL/skills" | python3 -m json.tool)
echo "$skills"
echo ""

# Wait a moment
sleep 1

echo -e "${BLUE}═══════════════════════════════════════════${NC}"
echo -e "${BLUE}    Running Skill Tests${NC}"
echo -e "${BLUE}═══════════════════════════════════════════${NC}"
echo ""

# Test CalculatorSkill
test_skill "CalculatorSkill" "calculate: 15 + 27"

# Test MockSearchSkill
test_skill "MockSearchSkill" "search: Spring Boot best practices"

# Test SummarizeSkill
test_skill "SummarizeSkill" "summarize: The quick brown fox jumps over the lazy dog. This is a pangram used for testing."

# Test WeatherSkill
test_skill "WeatherSkill" "weather in Seattle"

# Test OsqueryMCPSkill
test_skill "OsqueryMCPSkill" "what is the hostname"

echo -e "${GREEN}═══════════════════════════════════════════${NC}"
echo -e "${GREEN}✅ All skill tests completed!${NC}"
echo -e "${GREEN}═══════════════════════════════════════════${NC}"
echo ""
echo "📚 For more details, see: SKILLS_USAGE_GUIDE.md"
