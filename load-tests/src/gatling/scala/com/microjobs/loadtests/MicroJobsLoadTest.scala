package com.microjobs.loadtests

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class MicroJobsLoadTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling Load Test")

  // Test data
  val tenantIds = (1 to 100).map(i => s"tenant-$i").toArray
  val clientIds = (1 to 1000).map(i => s"client-$i").toArray
  val workerIds = (1 to 1000).map(i => s"worker-$i").toArray

  // Authentication scenario
  val authScenario = scenario("Authentication")
    .exec(
      http("Login Client")
        .post("/api/auth/login")
        .body(StringBody("""{
          "email": "client@demo.com",
          "password": "password123",
          "tenantId": "tenant-1"
        }"""))
        .check(status.is(200))
        .check(jsonPath("$.token").saveAs("authToken"))
    )

  // Job posting scenario
  val jobPostingScenario = scenario("Job Posting")
    .exec(authScenario)
    .repeat(5) {
      exec(
        http("Post Job")
          .post("/api/jobs")
          .header("Authorization", "Bearer ${authToken}")
          .body(StringBody("""{
            "title": "Load Test Job ${__Random(1,1000)}",
            "description": "This is a load test job for performance testing",
            "budget": {
              "amount": ${__Random(10000,100000)},
              "currency": "NGN"
            },
            "deadline": "2024-12-31T23:59:59",
            "requiredSkills": ["Java", "Spring Boot", "Microservices"],
            "location": "Lagos, Nigeria",
            "latitude": 6.5244,
            "longitude": 3.3792,
            "maxDistanceKm": 50
          }"""))
          .check(status.is(201))
          .check(jsonPath("$.id").saveAs("jobId"))
      )
    }

  // Bidding scenario
  val biddingScenario = scenario("Job Bidding")
    .exec(authScenario)
    .repeat(10) {
      exec(
        http("Submit Bid")
          .post("/api/jobs/${jobId}/bids")
          .header("Authorization", "Bearer ${authToken}")
          .body(StringBody("""{
            "bidAmount": {
              "amount": ${__Random(5000,50000)},
              "currency": "NGN"
            },
            "proposal": "I am an experienced developer with 5+ years in the field",
            "estimatedCompletionDays": ${__Random(7,30)}
          }"""))
          .check(status.is(201))
      )
    }

  // Job search scenario
  val searchScenario = scenario("Job Search")
    .exec(authScenario)
    .repeat(20) {
      exec(
        http("Search Jobs")
          .get("/api/jobs/search")
          .header("Authorization", "Bearer ${authToken}")
          .queryParam("q", "developer")
          .queryParam("location", "Lagos")
          .queryParam("maxDistance", "50")
          .queryParam("page", "0")
          .queryParam("size", "20")
          .check(status.is(200))
          .check(jsonPath("$.content").exists)
      )
    }

  // Escrow transaction scenario
  val escrowScenario = scenario("Escrow Transactions")
    .exec(authScenario)
    .repeat(3) {
      exec(
        http("Initiate Escrow")
          .post("/api/escrow/transactions")
          .header("Authorization", "Bearer ${authToken}")
          .body(StringBody("""{
            "jobId": "${jobId}",
            "clientId": "client-1",
            "workerId": "worker-1",
            "amount": {
              "amount": ${__Random(10000,50000)},
              "currency": "NGN"
            },
            "description": "Payment for job completion"
          }"""))
          .check(status.is(201))
          .check(jsonPath("$.id").saveAs("transactionId"))
      )
      .pause(2)
      .exec(
        http("Check Transaction Status")
          .get("/api/escrow/transactions/${transactionId}")
          .header("Authorization", "Bearer ${authToken}")
          .check(status.is(200))
      )
    }

  // Mixed workload scenario
  val mixedWorkloadScenario = scenario("Mixed Workload")
    .exec(authScenario)
    .randomSwitch(
      30.0 -> jobPostingScenario,
      25.0 -> biddingScenario,
      25.0 -> searchScenario,
      20.0 -> escrowScenario
    )

  // Load test configurations
  setUp(
    // Ramp up users over 2 minutes
    authScenario.inject(
      rampUsers(50).during(2.minutes)
    ),
    
    // Sustained load for 10 minutes
    mixedWorkloadScenario.inject(
      rampUsers(100).during(2.minutes),
      constantUsers(100).during(10.minutes),
      rampUsers(0).during(2.minutes)
    ),
    
    // Peak load test
    jobPostingScenario.inject(
      rampUsers(200).during(1.minutes),
      constantUsers(200).during(5.minutes),
      rampUsers(0).during(1.minutes)
    )
  )
  .protocols(httpProtocol)
  .assertions(
    global.responseTime.max.lt(2000), // Max response time < 2s
    global.responseTime.mean.lt(500), // Mean response time < 500ms
    global.responseTime.percentile3.lt(1000), // 95th percentile < 1s
    global.successfulRequests.percent.gt(95), // Success rate > 95%
    forAll.failedRequests.percent.lt(5) // Failure rate < 5%
  )

  // Additional scenarios for specific testing

  // Stress test scenario
  val stressTestScenario = scenario("Stress Test")
    .exec(authScenario)
    .exec(
      http("Heavy Job Search")
        .get("/api/jobs/search")
        .header("Authorization", "Bearer ${authToken}")
        .queryParam("q", "developer OR engineer OR programmer")
        .queryParam("location", "Lagos")
        .queryParam("maxDistance", "100")
        .queryParam("page", "0")
        .queryParam("size", "100")
        .check(status.is(200))
    )

  // Endurance test scenario
  val enduranceScenario = scenario("Endurance Test")
    .exec(authScenario)
    .exec(
      http("Get Job Details")
        .get("/api/jobs/${jobId}")
        .header("Authorization", "Bearer ${authToken}")
        .check(status.is(200))
    )
    .pause(1)
    .exec(
      http("Get Job Bids")
        .get("/api/jobs/${jobId}/bids")
        .header("Authorization", "Bearer ${authToken}")
        .check(status.is(200))
    )

  // Performance test configurations
  val performanceTest = setUp(
    stressTestScenario.inject(
      rampUsers(500).during(5.minutes),
      constantUsers(500).during(30.minutes),
      rampUsers(0).during(5.minutes)
    )
  ).protocols(httpProtocol)

  val enduranceTest = setUp(
    enduranceScenario.inject(
      rampUsers(50).during(1.minutes),
      constantUsers(50).during(60.minutes),
      rampUsers(0).during(1.minutes)
    )
  ).protocols(httpProtocol)
}
