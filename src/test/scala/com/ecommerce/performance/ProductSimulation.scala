package com.ecommerce.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class ProductSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .userAgentHeader("Gatling/Performance Test")

  val scn = scenario("Product API Test")
    .exec(
      http("Get All Products")
        .get("/api/products")
        .check(status.is(200))
    )
    .pause(1)
    .exec(
      http("Get Single Product")
        .get("/api/products/1")
        .check(status.is(200))
    )

  setUp(
    scn.inject(
      rampUsers(100).during(10.seconds),
      constantUsersPerSec(10).during(20.seconds)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(2000),
      global.successfulRequests.percent.gt(95)
    )
}