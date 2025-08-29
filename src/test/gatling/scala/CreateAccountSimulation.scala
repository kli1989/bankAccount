package gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

class CreateAccountSimulation extends Simulation {

  // HTTP Configuration
  val httpProtocol = http
    .baseUrl("http://localhost:8080/api/v1")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling Create Account Test")

  // Create account feeder with realistic test data
  val createAccountFeeder = Iterator.continually(Map(
    "accountNumber" -> f"ACC${Random.nextInt(900000) + 100000}%06d",
    "accountHolderName" -> s"Test User ${Random.nextInt(1000)}",
    "email" -> s"user${Random.nextInt(10000)}@test.com",
    "phoneNumber" -> s"+1${Random.nextInt(900) + 100}${Random.nextInt(900) + 100}${Random.nextInt(10000)}",
    "initialBalance" -> (Random.nextInt(9900) + 100),
    "currency" -> "USD"
  ))

  // Scenario: Create Account with Validation
  val createAccountScenario = scenario("Create Account Test")
    .feed(createAccountFeeder)
    .exec(
      http("Create Account - ${accountNumber}")
        .post("/accounts")
        .body(StringBody(
          """{
            "accountNumber": "${accountNumber}",
            "accountHolderName": "${accountHolderName}",
            "email": "${email}",
            "phoneNumber": "${phoneNumber}",
            "initialBalance": ${initialBalance},
            "currency": "${currency}"
          }""")).asJson
        .check(status.is(201))
        .check(jsonPath("$.id").exists)
        .check(jsonPath("$.accountNumber").is("${accountNumber}"))
        .check(jsonPath("$.balance").is("${initialBalance}"))
        .check(jsonPath("$.status").is("ACTIVE"))
        .check(responseTimeInMillis.lt(2000)) // Response time < 2 seconds
    )
    .pause(500.milliseconds, 1.second) // Random pause between requests

  // Scenario: Create Account with Error Handling
  val createAccountWithErrorsScenario = scenario("Create Account Error Test")
    .exec(
      // Test with duplicate account number (should fail)
      http("Create Account - Duplicate")
        .post("/accounts")
        .body(StringBody("""{
          "accountNumber": "DUPLICATE123",
          "accountHolderName": "Duplicate User",
          "email": "duplicate@test.com",
          "phoneNumber": "+1234567890",
          "initialBalance": 1000,
          "currency": "USD"
        }""")).asJson
        .check(status.is(201))
    )
    .exec(
      http("Create Account - Same Duplicate")
        .post("/accounts")
        .body(StringBody("""{
          "accountNumber": "DUPLICATE123",
          "accountHolderName": "Another User",
          "email": "another@test.com",
          "phoneNumber": "+1234567891",
          "initialBalance": 500,
          "currency": "USD"
        }""")).asJson
        .check(status.is(400)) // Should fail with duplicate error
    )
    .pause(1.second)

  // Load Test Configuration
  setUp(
    // Main performance test
    createAccountScenario.inject(
      nothingFor(5.seconds),              // Warm-up
      atOnceUsers(2),                     // Start with 2 users
      rampUsers(10).during(20.seconds),   // Ramp up to 10 users over 20s
      constantUsersPerSec(5).during(60.seconds), // Maintain 5 users/sec for 1 min
      rampUsers(25).during(30.seconds),   // Ramp up to 25 users over 30s
      constantUsersPerSec(15).during(120.seconds) // Maintain 15 users/sec for 2 min
    ).protocols(httpProtocol),

    // Error handling test (smaller scale)
    createAccountWithErrorsScenario.inject(
      nothingFor(30.seconds),
      atOnceUsers(1),
      rampUsers(3).during(30.seconds)
    ).protocols(httpProtocol)

  ).assertions(
    // Global performance assertions
    global.responseTime.max.lt(3000),           // Max response time < 3 seconds
    global.responseTime.mean.lt(800),           // Average response time < 800ms
    global.responseTime.percentile3.lt(1500),   // 95th percentile < 1.5 seconds
    global.successfulRequests.percent.gt(90),   // Success rate > 90%

    // Specific assertions for create account
    forAll.responseTime.max.lt(2500),           // All requests < 2.5 seconds
    forAll.successfulRequests.percent.gt(85)    // Individual success rate > 85%
  )

  // Maximum duration for the entire test
  .maxDuration(10.minutes)

  // Uncomment to enable detailed logging
  /*
  .assertions(
    details("Create Account - ACC*").responseTime.mean.lt(500),
    details("Create Account - ACC*").successfulRequests.percent.gt(95)
  )
  */
}
