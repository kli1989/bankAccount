# 🚀 Gatling Performance Tests for Banking System

This directory contains Gatling performance tests specifically designed for testing the Banking System API endpoints.

## 📋 Prerequisites

- **Java 17+** installed
- **Maven 3.6+** installed
- **Banking System** running on `http://localhost:8080`
- **Scala** (automatically downloaded by Maven)

## 🏗️ Test Structure

```
src/test/gatling/
├── scala/
│   ├── BankingSystemSimulation.scala    # Comprehensive test suite
│   └── CreateAccountSimulation.scala     # Focused create account test
└── resources/
    └── gatling.conf                      # Gatling configuration
```

## 🎯 Available Tests

### 1. `CreateAccountSimulation` (Recommended)
- **Focus**: Create account endpoint performance
- **Load Pattern**: Gradual ramp-up from 2 to 25 users
- **Duration**: ~5 minutes
- **Assertions**: Response time < 2s, Success rate > 90%

### 2. `BankingSystemSimulation` (Comprehensive)
- **Focus**: Multiple endpoints (create + retrieve)
- **Load Pattern**: Complex ramp-up scenarios
- **Duration**: ~10+ minutes
- **Assertions**: Multiple performance metrics

## 🚀 Running Tests

### Quick Start (Recommended)
```bash
# Make sure banking system is running
mvn spring-boot:run &
sleep 10

# Run the focused create account test
mvn gatling:test -Dgatling.simulationClass=gatling.CreateAccountSimulation
```

### Using the Helper Script
```bash
# Make the script executable
chmod +x run-gatling-tests.sh

# Run with default settings
./run-gatling-tests.sh

# Run specific simulation
./run-gatling-tests.sh CreateAccountSimulation

# Run with custom parameters
./run-gatling-tests.sh CreateAccountSimulation 20 120
```

### Manual Execution
```bash
# Run specific test
mvn gatling:test -Dgatling.simulationClass=gatling.CreateAccountSimulation

# Run all tests
mvn gatling:test

# Run with custom base URL
mvn gatling:test -DbaseUrl=http://your-server:8080/api/v1
```

## 📊 Test Results

### Report Location
```
target/gatling/
├── createaccountsimulation-20250101T120000/  # Latest test results
│   ├── index.html                           # HTML Report (Open in browser)
│   ├── js/
│   └── style/
└── ...
```

### Viewing Results
```bash
# Open the latest report in browser
open target/gatling/$(ls -t target/gatling | head -1)/index.html

# Or manually navigate to the HTML file
```

## 📈 Performance Metrics

The tests measure:

### Response Times
- **Average**: Target < 500ms
- **95th Percentile**: Target < 1.5s
- **Maximum**: Target < 2.5s

### Throughput
- **Requests/Second**: Target > 5 req/s
- **Success Rate**: Target > 90%

### Error Handling
- **HTTP 201**: Successful account creation
- **HTTP 400**: Duplicate account (expected error)
- **HTTP 500**: Server errors (should be minimal)

## 🔧 Configuration

### Test Parameters
Edit in `CreateAccountSimulation.scala`:

```scala
// Change user load
constantUsersPerSec(15).during(120.seconds)  // 15 users/sec for 2 minutes

// Change response time assertions
global.responseTime.mean.lt(800)             // Average < 800ms

// Change success rate requirements
global.successfulRequests.percent.gt(95)     // Success rate > 95%
```

### HTTP Configuration
Edit in `src/test/resources/gatling.conf`:

```hocon
http {
  connectTimeout = 10000      # Connection timeout (ms)
  requestTimeout = 60000      # Request timeout (ms)
  maxConnectionsPerHost = 10  # Max connections per host
}
```

## 🎯 Test Scenarios

### Create Account Test Flow
1. **Generate Random Data**: Unique account numbers, names, emails
2. **Send POST Request**: `/api/v1/accounts` with JSON payload
3. **Validate Response**: HTTP 201, correct JSON structure
4. **Performance Checks**: Response time < 2 seconds
5. **Pause**: Random 500ms-1s between requests

### Error Handling Test
1. **Create First Account**: Should succeed
2. **Create Duplicate**: Should fail with HTTP 400
3. **Validate Error Response**: Proper error message

## 📊 Interpreting Results

### Key Metrics to Watch

#### Response Time Distribution
- **< 500ms**: Excellent performance
- **500ms - 1s**: Good performance
- **1s - 2s**: Acceptable performance
- **> 2s**: Needs optimization

#### Success Rate
- **> 98%**: Excellent reliability
- **95-98%**: Good reliability
- **90-95%**: Acceptable reliability
- **< 90%**: Needs investigation

#### Throughput
- **> 20 req/s**: High performance
- **10-20 req/s**: Good performance
- **5-10 req/s**: Acceptable performance
- **< 5 req/s**: Needs optimization

## 🚨 Troubleshooting

### Common Issues

#### "Connection refused"
```bash
# Make sure banking system is running
mvn spring-boot:run
```

#### "Simulation not found"
```bash
# Check the class name
mvn gatling:test -Dgatling.simulationClass=gatling.CreateAccountSimulation
```

#### "Java heap space"
```bash
# Increase JVM memory
export MAVEN_OPTS="-Xmx2g -Xms1g"
mvn gatling:test
```

#### High error rates
- Check server logs for errors
- Verify database connectivity
- Monitor server resources (CPU, memory)

## 🔄 CI/CD Integration

### Jenkins/GitLab CI Example
```yaml
stages:
  - test
  - performance

performance_test:
  stage: performance
  script:
    - mvn spring-boot:run &
    - sleep 30
    - mvn gatling:test -Dgatling.simulationClass=gatling.CreateAccountSimulation
  artifacts:
    paths:
      - target/gatling/
    expire_in: 1 week
```

## 📝 Best Practices

### Test Data
- Use unique account numbers for each request
- Generate realistic names and emails
- Test edge cases (duplicates, invalid data)

### Load Patterns
- Start with low load and gradually increase
- Include warm-up periods
- Test both steady load and spikes

### Assertions
- Set realistic performance targets
- Include both success and failure scenarios
- Monitor trends over time

### Reporting
- Save reports for historical comparison
- Set up dashboards for continuous monitoring
- Alert on performance degradation

## 🎯 Next Steps

1. **Run the tests** with your current setup
2. **Analyze results** and identify bottlenecks
3. **Optimize code** based on findings
4. **Set up monitoring** for production
5. **Create more test scenarios** for other endpoints

---

**Happy Performance Testing! 🚀📊**
