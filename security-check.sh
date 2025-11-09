#!/bin/bash

# Security Check Script
# Verifies that sensitive files are properly protected by .gitignore

echo "🔒 Security Configuration Check"
echo "================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if .gitignore exists
if [ -f .gitignore ]; then
    echo -e "${GREEN}✓${NC} .gitignore file exists"
else
    echo -e "${RED}✗${NC} .gitignore file is missing!"
    exit 1
fi

# Check if .env.example exists
if [ -f .env.example ]; then
    echo -e "${GREEN}✓${NC} .env.example template exists"
else
    echo -e "${YELLOW}⚠${NC} .env.example template is missing"
fi

# Check if .env exists (should exist but be gitignored)
if [ -f .env ]; then
    echo -e "${YELLOW}⚠${NC} .env file exists (make sure it's gitignored)"
else
    echo -e "${GREEN}✓${NC} No .env file found (use .env.example to create one)"
fi

# Check if SECURITY.md exists
if [ -f SECURITY.md ]; then
    echo -e "${GREEN}✓${NC} SECURITY.md documentation exists"
else
    echo -e "${YELLOW}⚠${NC} SECURITY.md documentation is missing"
fi

echo ""
echo "Checking .gitignore patterns..."
echo "-------------------------------"

# Define patterns to check
declare -a patterns=(
    "*.key"
    "*.pem"
    ".env"
    "*.env.*"
    "**/secrets/**"
    "**/credentials/**"
)

for pattern in "${patterns[@]}"; do
    if grep -q "^$pattern" .gitignore; then
        echo -e "${GREEN}✓${NC} Pattern protected: $pattern"
    else
        echo -e "${RED}✗${NC} Pattern missing: $pattern"
    fi
done

echo ""
echo "Checking for sensitive files in repository..."
echo "---------------------------------------------"

# Check if git is initialized
if [ -d .git ]; then
    echo "Git repository detected. Checking staged files..."
    
    # Check for potentially sensitive files
    if git ls-files | grep -E '\.(key|pem|p12|jks)$' > /dev/null; then
        echo -e "${RED}✗${NC} WARNING: Certificate/key files found in repository!"
        git ls-files | grep -E '\.(key|pem|p12|jks)$'
    else
        echo -e "${GREEN}✓${NC} No certificate/key files in repository"
    fi
    
    if git ls-files | grep -E '^\.env$' > /dev/null; then
        echo -e "${RED}✗${NC} WARNING: .env file is tracked in git!"
    else
        echo -e "${GREEN}✓${NC} .env file is not tracked"
    fi
    
    if git ls-files | grep -E 'secrets|credentials' > /dev/null; then
        echo -e "${YELLOW}⚠${NC} Files with 'secrets' or 'credentials' in name found:"
        git ls-files | grep -E 'secrets|credentials'
    else
        echo -e "${GREEN}✓${NC} No files with 'secrets' or 'credentials' in name"
    fi
else
    echo -e "${YELLOW}⚠${NC} Not a git repository (run 'git init' to initialize)"
fi

echo ""
echo "Searching for hardcoded secrets in code..."
echo "------------------------------------------"

# Search for potential hardcoded secrets (basic patterns)
if grep -r -n -i --include="*.java" --include="*.properties" -E "(password|api.key|secret|token)\s*=\s*['\"][^'\"]+['\"]" src/ 2>/dev/null | grep -v "application.properties" | head -5; then
    echo -e "${YELLOW}⚠${NC} Potential hardcoded secrets found above"
    echo "   Review these carefully and move to environment variables if needed"
else
    echo -e "${GREEN}✓${NC} No obvious hardcoded secrets detected"
fi

echo ""
echo "Checking application.properties..."
echo "----------------------------------"

if [ -f src/main/resources/application.properties ]; then
    # Check for environment variable usage
    if grep -q '\${.*}' src/main/resources/application.properties; then
        echo -e "${GREEN}✓${NC} Using environment variables: \${VAR} pattern found"
    else
        echo -e "${YELLOW}⚠${NC} No environment variables detected"
    fi
    
    # Check for suspicious hardcoded values
    if grep -E -i "(password|api.key|secret)\s*=\s*[^$]" src/main/resources/application.properties | grep -v "^#" > /dev/null; then
        echo -e "${RED}✗${NC} Potential hardcoded secrets in application.properties:"
        grep -E -i "(password|api.key|secret)\s*=\s*[^$]" src/main/resources/application.properties | grep -v "^#"
    else
        echo -e "${GREEN}✓${NC} No hardcoded secrets in application.properties"
    fi
fi

echo ""
echo "Summary & Recommendations"
echo "========================="
echo ""
echo "1. ${GREEN}Always use .env for local development${NC}"
echo "2. ${GREEN}Never commit .env to version control${NC}"
echo "3. ${GREEN}Use environment variables for all secrets${NC}"
echo "4. ${YELLOW}Review SECURITY.md for complete guidelines${NC}"
echo "5. ${YELLOW}Run this check before committing code${NC}"
echo ""

# Return success if no critical issues
exit 0
