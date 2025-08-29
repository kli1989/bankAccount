package gatling

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.util.Random

class BankingSystemSimulation extends Simulation {

  // HTTP Configuration
  val httpProtocol = http
    .baseUrl("http://localhost:8080/api/v1") // Base URL for the banking system
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling Performance Test")

  // Random data generators
  val accountNumbers = Iterator.continually(
    Map("accountNumber" -> f"ACC${Random.nextInt(900000) + 100000}%06d")
  )

  val accountHolders = Iterator.continually(
    Map("accountHolder" -> s"Test User ${Random.nextInt(1000)}")
  )

  val emails = Iterator.continually(
    Map("email" -> s"user${Random.nextInt(10000)}@test.com")
  )

  val phones = Iterator.continually(
    Map("phone" -> s"+1${Random.nextInt(900) + 100}${Random.nextInt(900) + 100}${Random.nextInt(10000)}")
  )

  val balances = Iterator.continually(
    Map("balance" -> (Random.nextInt(9900) + 100).toString) // Random balance between 100-10000
  )

  // Create account request body feeder
  val createAccountFeeder = Iterator.continually(Map(
    "accountNumber" -> f"ACC${Random.nextInt(900000) + 100000}%06d",
    "accountHolderName" -> s"Test User ${Random.nextInt(1000)}",
    "email" -> s"user${Random.nextInt(10000)}@test.com",
    "phoneNumber" -> s"+1${Random.nextInt(900) + 100}${Random.nextInt(900) + 100}${Random.nextInt(10000)}",
    "initialBalance" -> (Random.nextInt(9900) + 100),
    "currency" -> "USD"
  ))

  // Scenario: Create Account
  val createAccountScenario = scenario("Create Account Performance Test")
    .feed(createAccountFeeder)
    .exec(
      http("Create New Account")
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
        .check(jsonPath("$.id").saveAs("accountId"))
        .check(jsonPath("$.accountNumber").is("${accountNumber}"))
        .check(responseTimeInMillis.lt(1000)) // Assert response time < 1 second
    )
    .pause(1.second, 3.seconds) // Random pause between 1-3 seconds

  // Scenario: Create and Retrieve Account
  val createAndRetrieveScenario = scenario("Create and Retrieve Account")
    .feed(createAccountFeeder)
    .exec(
      http("Create Account")
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
        .check(jsonPath("$.accountNumber").saveAs("createdAccountNumber"))
    )
    .pause(500.milliseconds)
    .exec(
      http("Retrieve Created Account")
        .get("/accounts/number/${createdAccountNumber}")
        .check(status.is(200))
        .check(jsonPath("$.accountNumber").is("${createdAccountNumber}"))
        .check(jsonPath("$.balance").is("${initialBalance}"))
    )

  // Ramp-up test configuration
  setUp(
    // Scenario 1: Gradual ramp-up test
    createAccountScenario.inject(
      nothingFor(5.seconds), // Warm-up period
      atOnceUsers(5),        // Start with 5 users
      rampUsers(20).during(30.seconds),  // Ramp up to 20 users over 30 seconds
      constantUsersPerSec(10).during(60.seconds), // Maintain 10 users/sec for 1 minute
      rampUsers(50).during(30.seconds),  // Ramp up to 50 users over 30 seconds
      constantUsersPerSec(25).during(120.seconds) // Maintain 25 users/sec for 2 minutes
    ).protocols(httpProtocol),

    // Scenario 2: Peak load test (optional - can be enabled separately)
    // createAndRetrieveScenario.inject(
    //   nothingFor(10.seconds),
    //   rampUsers(100).during(60.seconds),
    //   constantUsersPerSec(50).during(180.seconds)
    // ).protocols(httpProtocol)

  ).assertions(
    // Performance assertions
    global.responseTime.max.lt(2000),        // Max response time < 2 seconds
    global.responseTime.mean.lt(500),        // Average response time < 500ms
    global.responseTime.percentile3.lt(1000), // 95th percentile < 1 second
    global.successfulRequests.percent.gt(95), // Success rate > 95%

    // Throughput assertions
    global.requestsPerSec.gt(5)              // At least 5 requests per second
  )

  // Uncomment for detailed assertions per request
  /*
  .assertions(
    details("Create New Account").responseTime.max.lt(1500),
    details("Create New Account").responseTime.mean.lt(300),
    details("Create New Account").successfulRequests.percent.gt(98)
  )
  */
}
