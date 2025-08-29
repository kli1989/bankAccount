# Banking System API

A robust Spring Boot application for managing bank accounts with comprehensive REST API, fund transfer capabilities, and production-ready features.

## 🚀 Features

- **Account Management**: Create, read, update, and delete bank accounts
- **Fund Transfers**: Secure money transfers between accounts with concurrency control
- **Pagination & Search**: Efficient data retrieval with pagination and search capabilities
- **Caching**: Performance optimization with caching for frequently accessed data
- **Validation**: Comprehensive input validation and error handling
- **Testing**: Complete test suite including unit, integration, and stress tests
- **Containerization**: Docker support for easy deployment
- **Monitoring**: Health checks and metrics endpoints

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
- **Testing**: JUnit 5, Mockito, TestContainers
- **Documentation**: OpenAPI/Swagger
- **Caching**: Spring Cache with Caffeine

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

#### Utility Endpoints

**Get Account Balance**
```http
GET /api/v1/accounts/balance/{accountNumber}
```

**Check Account Existence**
```http
GET /api/v1/accounts/exists/{accountNumber}
```

**Get Active Account Count**
```http
GET /api/v1/accounts/count/active
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

## 🧪 Testing

### Run All Tests
```bash
./mvnw test
```

### Run Specific Test Categories

**Unit Tests**:
```bash
./mvnw test -Dtest="*Test"
```

**Integration Tests**:
```bash
./mvnw test -Dtest="*IntegrationTest"
```

**Stress Tests**:
```bash
./mvnw test -Dtest="*StressTest"
```

### Test Coverage Report
```bash
./mvnw jacoco:report
```
View report at: `target/site/jacoco/index.html`

### Manual Testing with cURL

**Create Account**:
```bash
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "1234567890",
    "accountHolderName": "John Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "initialBalance": 1000.00,
    "currency": "USD"
  }'
```

**Transfer Funds**:
```bash
curl -X POST http://localhost:8080/api/v1/accounts/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountNumber": "1234567890",
    "toAccountNumber": "0987654321",
    "amount": 500.00,
    "description": "Test transfer"
  }'
```

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

### Prometheus Metrics
```bash
curl http://localhost:8080/api/v1/actuator/prometheus
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

### Cache Configuration
```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=500,expireAfterAccess=600s
```

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

## 📈 Performance Optimization

### Caching Strategy
- Account data cached for 5 minutes
- Balance information cached for 1 minute
- Cache eviction on data modifications

### Database Optimization
- Indexed queries for account number lookups
- Pagination for large result sets
- Connection pooling with HikariCP

### Concurrent Processing
- Pessimistic locking for critical operations
- Thread-safe balance updates
- Optimized database queries

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Troubleshooting

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

### Support
For support and questions:
- Check the issues section on GitHub
- Review the API documentation
- Run health checks to verify system status

---

**Happy Banking! 🏦💰**
