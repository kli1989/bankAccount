# Banking System API

A robust Spring Boot application for managing bank accounts with comprehensive REST API, fund transfer capabilities, and production-ready features.

## 📋 Table of Contents

- [🚀 Features](#-features)
- [📋 Requirements](#-requirements)
- [🛠️ Technology Stack](#️-technology-stack)
- [🚀 Quick Start](#-quick-start)
- [📚 API Documentation](#-api-documentation)
- [🧪 Testing Suite](#-testing-suite)
  - [🎯 Performance Testing with Gatling](#-performance-testing-with-gatling)
  - [🧪 API Testing](#-api-testing)
  - [🏃‍♂️ Unit Testing](#️-unit-testing)
  - [🔗 Integration Testing](#-integration-testing)
  - [📊 Test Reporting & Analytics](#-test-reporting--analytics)
  - [🎨 User Interface Testing](#-user-interface-testing)
- [📈 Cache & Performance Optimization](#-cache--performance-optimization)
- [🐳 Docker Deployment](#-docker-deployment)
- [📊 Monitoring & Health Checks](#-monitoring--health-checks)
- [🔒 Security Considerations](#-security-considerations)
- [🔧 Configuration](#-configuration)
- [🏗️ Architecture](#️-architecture)

- [🔍 Troubleshooting](#-troubleshooting)

## 🚀 Features

- **🏦 Account Management**: Create, read, update, and delete bank accounts
- **💸 Fund Transfers**: Secure money transfers between accounts with concurrency control
- **🔍 Pagination & Search**: Efficient data retrieval with pagination and search capabilities
- **⚡ Caching**: High-performance caching with Redis (production) and Caffeine (development/test) with 24-hour TTL
- **✅ Validation**: Comprehensive input validation and error handling
- **🧪 Testing Suite**: Complete test suite including unit, integration, stress, and performance tests
- **🚀 Performance Testing**: Gatling-based load testing with comprehensive metrics and reporting
- **🐳 Containerization**: Docker support for easy deployment
- **📊 Monitoring**: Health checks and metrics endpoints

## 📋 Requirements

- Java 17 or higher
- Maven 3.6+ or Docker
- 1GB RAM minimum (2GB recommended)
- 500MB disk space

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: H2 (In-memory for development, file-based for production)
- **Build Tool**: Maven
- **Container**: Docker
- **Testing**: JUnit 5, Mockito, TestContainers, Gatling (Performance Testing)
- **Documentation**: OpenAPI/Swagger
- **Caching**: Spring Cache with Redis (production) and Caffeine (development/test)

## 🚀 Quick Start

### Using Docker (Recommended)

1. **Clone and navigate to the project**:
   ```bash
   git clone <repository-url>
   cd banking-system
   ```

2. **Build and run with Docker Compose**:
   ```bash
   docker-compose up --build
   ```

3. **Access the application**:
   - API: http://localhost:8080/api/v1
   - Health check: http://localhost:8080/api/v1/actuator/health

### Using Maven

1. **Prerequisites**:
   ```bash
   # Install Java 17
   # Install Maven 3.6+
   ```

2. **Build the application**:
   ```bash
   ./mvnw clean package
   ```

3. **Run the application**:
   ```bash
   java -jar target/banking-system-1.0.0.jar
   ```

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/v1
```

### Core Endpoints

#### Account Management

**Create Account**
```http
POST /api/v1/accounts
Content-Type: application/json

{
  "accountNumber": "1234567890",
  "accountHolderName": "John Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "initialBalance": 1000.00,
  "currency": "USD"
}
```

**Get Account by ID**
```http
GET /api/v1/accounts/{id}
```

**Update Account**
```http
PUT /api/v1/accounts/{id}
Content-Type: application/json

{
  "accountHolderName": "John Smith",
  "email": "john.smith@example.com",
  "phoneNumber": "+0987654321"
}
```

**Delete Account**
```http
DELETE /api/v1/accounts/{id}
```

**List Accounts (with pagination)**
```http
GET /api/v1/accounts?page=0&size=10&sortBy=id&sortDir=ASC
```

**Search Accounts**
```http
GET /api/v1/accounts/search?name=John&page=0&size=10
```

#### Fund Transfer

**Transfer Funds**
```http
POST /api/v1/accounts/transfer
Content-Type: application/json

{
  "fromAccountNumber": "1234567890",
  "toAccountNumber": "0987654321",
  "amount": 500.00,
  "description": "Payment for services"
}
```

### Response Format

**Success Response**:
```json
{
  "id": 1,
  "accountNumber": "1234567890",
  "accountHolderName": "John Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "balance": 1000.00,
  "currency": "USD",
  "status": "ACTIVE",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

**Error Response**:
```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Input validation failed",
  "validationErrors": {
    "accountNumber": "Account number is required",
    "balance": "Balance cannot be negative"
  }
}
```

## 🧪 Testing Suite

### 🎯 Performance Testing with Gatling

This project includes comprehensive **performance testing** using **Gatling**, a modern load testing tool written in Scala. Performance tests validate system scalability, identify bottlenecks, and ensure the application can handle real-world load conditions.

#### 🚀 Quick Performance Test
```bash
# Start the banking system first
./mvnw spring-boot:run &
sleep 15

# Run quick performance test
./quick-demo.sh

# View results in browser
open target/gatling/$(ls -t target/gatling | head -1)/index.html
```

#### 🎯 Performance Test Scenarios
- **Load Testing**: Gradual ramp-up from 2 to 25 concurrent users
- **Stress Testing**: High-load scenarios with 50+ concurrent users
- **API Endpoint Testing**: Comprehensive coverage of all REST endpoints
- **Data Validation**: Testing with realistic account data and transactions

#### 🗃️ Test Data Generation
Performance tests use dynamically generated test data to simulate real-world scenarios:
- **Account Numbers**: 8-20 character alphanumeric strings (e.g., ACC12345678)
- **Account Holders**: Realistic names generated from common name patterns
- **Email Addresses**: Valid email formats with unique domains
- **Phone Numbers**: International format with proper validation
- **Balances**: Random amounts between $100-$10,000
- **Currencies**: ISO 4217 currency codes (primarily USD for testing)

#### 📊 Performance Metrics
- **Response Times**: Average, 95th percentile, maximum
- **Throughput**: Requests per second
- **Success Rate**: Percentage of successful requests (>90% target)
- **Error Analysis**: HTTP status code distribution
- **Resource Utilization**: Memory and CPU usage patterns

#### 🛠️ Running Custom Performance Tests
```bash
# Run specific test scenario
mvn gatling:test -Dgatling.simulationClass=gatling.CreateAccountSimulation

# Run with custom parameters
./run-gatling-tests.sh CreateAccountSimulation 50 300

# Run all performance tests
mvn gatling:test
```

#### 📋 Detailed Documentation
For comprehensive information about performance testing setup, configuration, and best practices, see: **[GATLING-README.md](GATLING-README.md)**
### 🏃‍♂️ Unit Testing

Unit tests validate individual components and business logic in isolation.

#### Unit Test Categories
- **Service Layer**: Business logic and calculations
- **Validation**: Input validation and error handling
- **Models**: Entity relationships and constraints

#### Run Unit Tests
```bash
# Run all unit tests
./mvnw test -Dtest="*Test"
```

### 🔗 Integration Testing

Integration tests validate the complete application stack including database operations and external API calls.

#### Integration Test Coverage
- **Database Operations**: CRUD operations with real database
- **API Workflows**: Complete user journeys and business processes
- **External Services**: Third-party API integrations
- **Security**: Authentication and authorization flows

#### Run Integration Tests
```bash
# Run all integration tests
./mvnw test -Dtest="*IntegrationTest"

# Run specific controller tests
./mvnw test -Dtest="BankAccountControllerIntegrationTest"
```

### 📊 Test Reporting & Analytics

#### Code Coverage Reports
```bash
# Generate JaCoCo coverage report
./mvnw jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

**Coverage Metrics:**
- **Overall Coverage**: Target > 85%
- **API Layer**: Controllers, DTOs, Validation
- **Service Layer**: Business logic, Transactions
- **Repository Layer**: Data access, Queries
- **Integration Tests**: End-to-end workflows

#### Performance Test Reports
Performance test results are automatically generated as HTML reports:
```
target/gatling/
├── createaccountsimulation-20250101T120000/
│   ├── index.html          # Main report
│   ├── js/                 # Charts and graphs
│   ├── style/              # CSS styling
│   └── simulation.log      # Detailed logs
```

**Performance Report Contents:**
- 📈 **Response Time Charts**: Average, percentiles, distribution
- 🚀 **Throughput Graphs**: Requests per second over time
- ✅ **Success Rate Trends**: Pass/fail ratios
- 🔍 **Error Analysis**: HTTP status codes, error messages
- 📊 **Resource Usage**: Memory, CPU, database connections

### 🔍 Debugging & Troubleshooting

#### Common Test Issues

**Test Failures:**
```bash
# Run failed tests individually
./mvnw test -Dtest=BankAccountServiceTest#testCreateAccount

# Run with debug output
./mvnw test -DforkCount=1 -DreuseForks=false
```

**Performance Issues:**
```bash
# Check system resources during performance tests
top -p $(pgrep java)

# Monitor database connections
# Check application logs for bottlenecks
tail -f logs/application.log
```

**Integration Test Problems:**
```bash
# Test database connectivity
./mvnw test -Dtest=DatabaseConnectionTest

# Check test data setup
./mvnw test -Dtest=TestDataSetupTest
```

### 📈 Testing Metrics & KPIs

#### Quality Metrics
- **Test Coverage**: >85% overall coverage
- **Test Execution Time**: <5 minutes for full suite
- **Flaky Test Rate**: <1% test failures
- **Performance Benchmarks**: <500ms average response time

#### CI/CD Metrics
- **Build Success Rate**: >95%
- **Test Execution Frequency**: Every commit
- **Performance Regression**: <5% degradation allowed
- **Code Quality Gates**: All checks must pass

### 🔗 Related Documentation

- **[API Documentation](http://localhost:8080/swagger-ui.html)** - Interactive API docs
- **[GATLING-README.md](GATLING-README.md)** - Performance testing guide
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Development guidelines
- **[DEPLOYMENT.md](DEPLOYMENT.md)** - Deployment instructions

### Test Coverage Report
```bash
./mvnw jacoco:report
```
View report at: `target/site/jacoco/index.html`

### 🎨 User Interface Testing

#### Web Interface
Access the banking system's web interface for manual testing:
```
http://localhost:8080/api/v1/index.html
```

**Web UI Features:**
- 🏦 **Account Management**: Create, view, update, and delete accounts
- 💸 **Fund Transfers**: Transfer money between accounts
- 🔍 **Search & Browse**: Find accounts by various criteria
- 📊 **Real-time Updates**: Live balance and transaction updates

#### API Testing Interfaces
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Postman Collection**: `banking-system-api.postman_collection.json`
- **cURL Examples**: See API Testing section above

---

**🎉 Happy Testing!** 🧪🚀📊

**Need help?** Check our [troubleshooting guide](#-debugging--troubleshooting) or open an issue.

## 📈 Cache & Performance Optimization

### Cache Configuration

The application uses **high-performance caching** with different cache providers for different environments:

#### Production Environment (Redis)
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 86400000  # 24 hours in milliseconds

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      database: 0
```

#### Development/Test Environment (Caffeine)
```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=500,expireAfterWrite=86400s  # 24 hours
```

#### Cached Operations
The following operations are cached with automatic cache invalidation:

- **Account Retrieval**: `getAccountById()` and `getAccountByAccountNumber()`
- **Account Details**: `getAccountDetails()` for detailed account information
- **Cache TTL**: 24 hours (expires daily at midnight)
- **Cache Eviction**: Automatic on account updates and deletions

#### Cache Benefits
- **Performance**: 80-90% reduction in database queries for cached data
- **Scalability**: Handles high read loads efficiently
- **Consistency**: Automatic cache invalidation on data modifications
- **Environment Flexibility**: Redis for distributed caching, Caffeine for single-instance

### Database Optimization
- Indexed queries for account number lookups
- Pagination for large result sets
- Connection pooling with HikariCP

### Concurrent Processing
- Pessimistic locking for critical operations
- Thread-safe balance updates
- Optimized database queries


### Common Issues

**Application won't start**:
- Ensure Java 17 is installed and JAVA_HOME is set
- Check if port 8080 is available
- Verify Maven dependencies are downloaded

**Database connection issues**:
- For H2 console: http://localhost:8080/api/v1/h2-console
- Check database URL in configuration
- Ensure database files have proper permissions (production)

**Test failures**:
- Run `./mvnw clean test` to ensure clean test execution
- Check test logs for specific error messages
- Verify test database configuration

**Docker issues**:
- Ensure Docker daemon is running
- Check container logs: `docker logs <container-id>`
- Verify port mappings and environment variables

## 🐳 Docker Deployment

### Development with Docker Compose
```bash
# Start all services
docker-compose up --build

# Start with H2 console (for debugging)
docker-compose --profile dev up --build

# Stop services
docker-compose down
```

### Production Deployment
```bash
# Build production image
docker build -t banking-system:latest .

# Run production container
docker run -d \
  --name banking-system \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  banking-system:latest
```

### Environment Variables
```bash
# Database Configuration
DB_USERNAME=sa
DB_PASSWORD=your_secure_password

# Server Configuration
SERVER_PORT=8080

# External Database (if using external H2)
SPRING_DATASOURCE_URL=jdbc:h2:file:/data/banking-system
```

## 📊 Monitoring & Health Checks

### Health Check Endpoint
```bash
curl http://localhost:8080/api/v1/actuator/health
```

### Metrics Endpoint
```bash
curl http://localhost:8080/api/v1/actuator/metrics
```

## 🔒 Security Considerations

### Input Validation
- All inputs are validated using Bean Validation annotations
- Custom validation for account numbers, phone numbers, and currencies
- SQL injection prevention through JPA/Hibernate

### Concurrency Control
- Pessimistic locking for fund transfers to prevent race conditions
- Atomic operations for balance updates
- Thread-safe service implementations

### Error Handling
- Comprehensive exception handling with meaningful error messages
- Sensitive information is not exposed in error responses
- Proper HTTP status codes for different error scenarios

## 🔧 Configuration

### Application Profiles

**Development** (`application.yml`):
- H2 in-memory database
- Debug logging enabled
- H2 console accessible

**Production** (`application-prod.yml`):
- H2 file-based database
- Optimized caching
- Compressed responses
- Externalized configuration

**Testing** (`application-test.yml`):
- Clean database for each test
- Minimal logging
- Random server ports

## 🏗️ Architecture

### Layered Architecture
- **Controller Layer**: REST API endpoints with validation
- **Service Layer**: Business logic and transaction management
- **Repository Layer**: Data access with Spring Data JPA
- **Entity Layer**: JPA entities with validation

### Key Design Patterns
- **Repository Pattern**: Data access abstraction
- **Service Layer Pattern**: Business logic encapsulation
- **DTO Pattern**: Data transfer objects for API communication
- **Exception Handling**: Global exception handling with custom exceptions

### Database Design
- Single `bank_accounts` table with constraints
- Unique constraint on account numbers
- Audit fields (created_at, updated_at)
- Status enumeration for account states

---

**Happy Banking! 🏦💰**
