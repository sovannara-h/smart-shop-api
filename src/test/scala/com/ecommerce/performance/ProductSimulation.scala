package com.ecommerce.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class ProductSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .userAgentHeader("Gatling/Performance Test")

  val productFeeder = Iterator.continually(Map(
    "name" -> s"Product ${scala.util.Random.nextInt(1000)}",
    "price" -> scala.util.Random.nextDouble() * 1000,
    "description" -> s"Description for product ${scala.util.Random.nextInt(1000)}",
    "stockInQuantity" -> scala.util.Random.nextInt(100)
  ))

  val browseScenario = scenario("Browse Products")
    .exec(
      http("Get All Products - Page 0")
        .get("/api/products?page=0&size=10")
        .check(status.is(200))
        .check(jsonPath("$.data.content[0].id").saveAs("productId"))
    )
    .exitHereIfFailed // Ajout de cette ligne
    .pause(500.milliseconds)
    .exec(
      http("Get Single Product")
        .get("/api/products/#{productId}")  // Changement de ${} à #{}
        .check(status.is(200))
    )
    .pause(500.milliseconds)
    .exec(
      http("Get All Products - Page 1")
        .get("/api/products?page=1&size=10")
        .check(status.is(200))
    )

  val createScenario = scenario("Create Products")
    .feed(productFeeder)
    .exec(
      http("Create New Product")
        .post("/api/products")
        .body(StringBody("""{"name":"${name}","price":${price},"description":"${description}","stockInQuantity":${stockInQuantity}}"""))
        .check(status.in(201, 429))
    )

  val mixedScenario = scenario("Mixed Operations")
    .exec(browseScenario)
    .pause(500.milliseconds)
    .exec(createScenario)

  setUp(
    // Test de charge extrême pour la navigation
    browseScenario.inject(
      nothingFor(2.seconds),
      atOnceUsers(1000),                    // 1000 utilisateurs instantanés
      rampUsers(5000).during(30.seconds),   // +5000 sur 30s
      constantUsersPerSec(200).during(2.minutes)  // 200 users/sec pendant 2min
    ),
    // Test de pic pour les créations
    createScenario.inject(
      nothingFor(10.seconds),
      atOnceUsers(500),                     // 500 créations simultanées
      rampUsers(1000).during(20.seconds),   // +1000 sur 20s
      constantUsersPerSec(50).during(1.minute)  // 50 créations/sec pendant 1min
    ),
    // Test mixte intensif
    mixedScenario.inject(
      nothingFor(5.seconds),
      rampUsers(2000).during(30.seconds),   // 2000 utilisateurs mixtes
      constantUsersPerSec(100).during(90.seconds)  // 100 ops mixtes/sec pendant 90s
    )
  ).protocols(httpProtocol)
   .assertions(
      global.responseTime.max.lt(10000),    // Max 10s
      global.responseTime.mean.lt(3000),    // Moyenne < 3s
      global.successfulRequests.percent.gt(95),  // 95% de succès
      details("Get All Products - Page 0").responseTime.percentile3.lt(5000)
   )
}