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

  // Données de test pour la création de produits
  val productFeeder = Iterator.continually(Map(
    "name" -> s"Product ${scala.util.Random.nextInt(1000)}",
    "price" -> scala.util.Random.nextDouble() * 1000,
    "description" -> s"Description for product ${scala.util.Random.nextInt(1000)}",
    "stockInQuantity" -> scala.util.Random.nextInt(100)
  ))

  // Scénario de navigation basique
  val browseScenario = scenario("Browse Products")
    .exec(
      http("Get All Products - Page 0")
        .get("/api/products?page=0&size=10")
        .check(status.is(200))
        .check(jsonPath("$.data.content[0].id").saveAs("productId"))
    )
    .pause(1)
    .exec(
      http("Get Single Product")
        .get("/api/products/${productId}")
        .check(status.is(200))
    )
    .pause(1)
    .exec(
      http("Get All Products - Page 1")
        .get("/api/products?page=1&size=10")
        .check(status.is(200))
    )

  // Scénario de création de produits
  val createScenario = scenario("Create Products")
    .feed(productFeeder)
    .exec(
      http("Create New Product")
        .post("/api/products")
        .body(StringBody("""{"name":"${name}","price":${price},"description":"${description}","stockInQuantity":${stockInQuantity}}"""))
        .check(status.in(201, 429))
    )

  // Scénario mixte avec différentes opérations
  val mixedScenario = scenario("Mixed Operations")
    .exec(browseScenario)
    .pause(1)
    .exec(createScenario)

  setUp(
    // Test de montée en charge massive
    browseScenario.inject(
      rampUsers(500).during(30.seconds),  // 500 utilisateurs sur 30s
      constantUsersPerSec(50).during(2.minutes) // 50 utilisateurs/sec pendant 2min
    ),
    // Test de pic de charge intense
    createScenario.inject(
      nothingFor(30.seconds),
      atOnceUsers(200),  // 200 utilisateurs d'un coup
      rampUsers(300).during(30.seconds)  // +300 sur 30s
    ),
    // Test de charge constante élevée
    mixedScenario.inject(
      constantUsersPerSec(20).during(3.minutes)  // 20 utilisateurs/sec pendant 3min
    )
  ).protocols(httpProtocol)
   .assertions(
      global.responseTime.max.lt(5000),    // Max 5s
      global.responseTime.mean.lt(2000),   // Moyenne < 2s
      global.successfulRequests.percent.gt(90),  // 90% de succès
      details("Get All Products - Page 0").responseTime.percentile3.lt(3000)
   )
}