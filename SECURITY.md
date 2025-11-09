# Security Guidelines

**Project**: Spring AI Agent Demo  
**Last Updated**: November 8, 2025

---

## Table of Contents
1. [Overview](#overview)
2. [Sensitive Data Protection](#sensitive-data-protection)
3. [Configuration Management](#configuration-management)
4. [API Security](#api-security)
5. [MCP Server Security](#mcp-server-security)
6. [Best Practices](#best-practices)
7. [Security Checklist](#security-checklist)

---

## Overview

This document outlines security practices for the Spring AI Agent Demo application to protect sensitive data, API keys, and credentials.

---

## Sensitive Data Protection

### What NOT to Commit

❌ **Never commit to Git**:
- API keys and tokens
- Passwords and secrets
- Private keys (.key, .pem files)
- Database credentials
- OAuth tokens
- Environment-specific configurations with sensitive data
- Keystore and truststore files
- SSH keys
- Cloud provider credentials (AWS, Azure, GCP)

### Protected by .gitignore

The `.gitignore` file protects:

```
# Keys and certificates
*.key
*.pem
*.p12
*.jks

# Environment files
.env
.env.*

# Credentials
**/secrets/**
**/credentials/**

# Application properties with secrets
application-local.properties
application-secrets.properties
```

---

## Configuration Management

### Using Environment Variables

**Best Practice**: Store sensitive configuration in environment variables, not in `application.properties`.

#### 1. Create a .env file (gitignored)

```bash
# Copy template
cp .env.example .env

# Edit with your actual values
nano .env
```

#### 2. Reference in application.properties

```properties
# Instead of hardcoding:
# openai.api.key=sk-actual-key-here

# Use environment variable:
openai.api.key=${OPENAI_API_KEY}
```

#### 3. Load .env in development

**Option A**: Use Spring Boot DevTools (automatically loads .env)

**Option B**: Use dotenv library
```xml
<dependency>
    <groupId>me.paulschwarz</groupId>
    <artifactId>spring-dotenv</artifactId>
    <version>4.0.0</version>
</dependency>
```

**Option C**: Export manually before running
```bash
export $(cat .env | xargs)
mvn spring-boot:run
```

### Configuration Files

**Public Configuration** (safe to commit):
- `application.properties` - Default settings, no secrets
- `application-dev.properties` - Dev settings template
- `application-prod.properties` - Prod settings template

**Private Configuration** (never commit):
- `.env` - Environment variables with actual values
- `application-local.properties` - Local overrides with secrets
- `application-secrets.properties` - Secret values

---

## API Security

### Current State

⚠️ **No authentication currently implemented**

The REST API (`/api/agent/execute`) is currently **open to all requests**.

### Recommended Security Enhancements

#### 1. Add Spring Security

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

#### 2. Implement JWT Authentication

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/agent/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
            .build();
    }
}
```

#### 3. Add API Key Authentication

```java
@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    @Value("${api.key}")
    private String apiKey;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        String requestApiKey = request.getHeader("X-API-Key");
        if (!apiKey.equals(requestApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
```

Store API key in `.env`:
```bash
API_KEY=your-secure-random-key-here
```

Reference in `application.properties`:
```properties
api.key=${API_KEY}
```

#### 4. Add Rate Limiting

```xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.5.0</version>
</dependency>
```

#### 5. Enable CORS Properly

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://yourdomain.com")  // Never use "*" in production
                .allowedMethods("POST", "GET")
                .maxAge(3600);
    }
}
```

---

## MCP Server Security

### osquery MCP Server Risks

⚠️ **Current Security Issues**:

1. **SQL Injection**: Accepts arbitrary SQL queries
2. **No Query Validation**: Any SQL can be executed
3. **No Authentication**: No verification of caller
4. **Process Access**: Can query sensitive system information

### Hardening osquery-mcp-server.py

#### 1. Whitelist Allowed Tables

```python
ALLOWED_TABLES = [
    'system_info',
    'processes',
    'users',
    'listening_ports',
    'interface_addresses'
]

def validate_query(sql: str) -> bool:
    """Only allow queries on whitelisted tables"""
    sql_lower = sql.lower()
    
    # Extract table name from SELECT
    import re
    match = re.search(r'from\s+(\w+)', sql_lower)
    if not match:
        return False
    
    table = match.group(1)
    return table in ALLOWED_TABLES

# In handle_call_tool()
if not validate_query(sql):
    return {"error": "Query not allowed: table not whitelisted"}
```

#### 2. Prevent Dangerous Operations

```python
FORBIDDEN_KEYWORDS = ['drop', 'delete', 'update', 'insert', 'create', 'alter']

def is_safe_query(sql: str) -> bool:
    sql_lower = sql.lower()
    return not any(keyword in sql_lower for keyword in FORBIDDEN_KEYWORDS)
```

#### 3. Add Query Templates

```python
# Safer: Use predefined templates instead of raw SQL
QUERY_TEMPLATES = {
    "system_info": "SELECT hostname, cpu_brand, physical_memory FROM system_info",
    "processes": "SELECT pid, name, state FROM processes LIMIT 100",
    "users": "SELECT username, uid, shell FROM users"
}

# Accept template name instead of raw SQL
template = arguments.get("template")
if template in QUERY_TEMPLATES:
    sql = QUERY_TEMPLATES[template]
else:
    return {"error": f"Unknown template: {template}"}
```

#### 4. Add Authentication Token

```python
import os

API_TOKEN = os.getenv("MCP_API_TOKEN", "")

def handle_call_tool(tool_name: str, arguments: dict) -> dict:
    # Verify token
    token = arguments.get("token", "")
    if token != API_TOKEN:
        return {"error": "Unauthorized: Invalid token"}
    
    # Rest of the logic...
```

Set token in `.env`:
```bash
MCP_API_TOKEN=your-secure-random-token
```

---

## Best Practices

### Development

✅ **DO**:
- Use `.env` files for local development
- Use environment variables for all sensitive data
- Keep `.env.example` updated with variable names (not values)
- Review `.gitignore` before committing
- Use `git status` to verify no secrets are staged
- Rotate API keys and secrets regularly
- Use different credentials for dev/staging/prod

❌ **DON'T**:
- Hardcode API keys in source code
- Commit `.env` files
- Share credentials in chat/email
- Use production credentials in development
- Copy/paste secrets in public channels

### Production

✅ **DO**:
- Use environment variables or secrets management services
- Use Azure Key Vault, AWS Secrets Manager, or HashiCorp Vault
- Enable HTTPS/TLS for all connections
- Implement authentication and authorization
- Monitor for security vulnerabilities
- Keep dependencies updated
- Use read-only database users where possible
- Implement proper logging (but don't log secrets)

❌ **DON'T**:
- Store secrets in plain text files on servers
- Use default passwords
- Expose internal services to the internet
- Disable security features "temporarily"

### Git Hygiene

**Before committing**:
```bash
# Check what will be committed
git status
git diff --cached

# Search for potential secrets
git diff --cached | grep -i "password\|api.key\|secret\|token"

# Use git-secrets to prevent commits
git secrets --scan
```

**Install git-secrets** (recommended):
```bash
# macOS
brew install git-secrets

# Configure
git secrets --install
git secrets --register-aws
git secrets --add 'password|api[_-]key|secret|token'
```

---

## Security Checklist

### Initial Setup

- [ ] Copy `.env.example` to `.env`
- [ ] Add actual secrets to `.env`
- [ ] Verify `.env` is in `.gitignore`
- [ ] Never commit `.env` file
- [ ] Configure environment variables in production

### Before Each Commit

- [ ] Run `git status` to check staged files
- [ ] Verify no `.env` or secret files are staged
- [ ] Search for hardcoded secrets in code
- [ ] Review diff for sensitive data
- [ ] Run tests to ensure config works

### Production Deployment

- [ ] Set environment variables on server
- [ ] Enable HTTPS/TLS
- [ ] Configure authentication/authorization
- [ ] Enable rate limiting
- [ ] Configure CORS properly
- [ ] Review MCP server security
- [ ] Enable security headers
- [ ] Configure firewall rules
- [ ] Set up monitoring and alerts
- [ ] Document secret rotation process

### Regular Maintenance

- [ ] Rotate API keys and secrets quarterly
- [ ] Review access logs for suspicious activity
- [ ] Update dependencies for security patches
- [ ] Audit `.gitignore` for completeness
- [ ] Review and update this security document

---

## Incident Response

### If Secrets Are Accidentally Committed

1. **Immediate Action**:
   ```bash
   # DON'T just delete the file and commit again
   # The secret is still in git history!
   
   # Rotate the compromised secret immediately
   # Generate new API key/password/token
   ```

2. **Remove from Git History**:
   ```bash
   # Use BFG Repo-Cleaner (recommended)
   git clone --mirror https://github.com/yourrepo/project.git
   java -jar bfg.jar --delete-files .env
   cd project.git
   git reflog expire --expire=now --all
   git gc --prune=now --aggressive
   git push --force
   ```

3. **Notify**:
   - Inform team members
   - Update secrets in all environments
   - Monitor for unauthorized access
   - Consider security audit if needed

### Reporting Security Issues

If you discover a security vulnerability:

1. **DO NOT** open a public GitHub issue
2. Email security contact: [your-email@example.com]
3. Include details but not the actual secret values
4. Wait for acknowledgment before disclosure

---

## Additional Resources

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/index.html)
- [Twelve-Factor App: Config](https://12factor.net/config)
- [GitHub Security Best Practices](https://docs.github.com/en/code-security)
- [git-secrets Tool](https://github.com/awslabs/git-secrets)

---

**Last Updated**: November 8, 2025  
**Review Frequency**: Quarterly or after security incidents
