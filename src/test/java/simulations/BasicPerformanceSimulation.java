package simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;

/**
 * Basic Performance Test Simulation in Java
 * Focuses on the core API endpoints with simple load patterns
 */
public class BasicPerformanceSimulation extends Simulation {

    // HTTP Protocol Configuration
    HttpProtocolBuilder httpProtocol = http
        .baseUrl("https://reqres.in/api")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .header("x-api-key", "reqres-free-v1");

    // Simple scenario: Get user by ID
    ScenarioBuilder getUserScenario = scenario("Get User by ID")
        .exec(
            http("Get User")
                .get("/users/2")
                .check(status().is(200))
                .check(jsonPath("$.data.id").is("2"))
        );

    // Load Simulation Setup
    {
        setUp(
            getUserScenario.injectOpen(
                // Start with 1 user, ramp up to 50 users over 30 seconds
                rampUsers(50).during(Duration.ofSeconds(30))
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().mean().lt(1000),           // Average response time < 1 second
             global().responseTime().percentile(95.0).lt(2000), // 95th percentile < 2 seconds
             global().successfulRequests().percent().gt(99.0)   // 99% success rate
         );
    }
}

