package simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;

/**
 * Gatling Load Testing Simulation for ReqRes API (Java Version)
 * This mirrors the functional tests in ApiTest.java but adds load testing capabilities
 */
public class ApiLoadSimulation extends Simulation {

    // HTTP Protocol Configuration - mirrors TestConfig.java
    HttpProtocolBuilder httpProtocol = http
        .baseUrl("https://reqres.in/api")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .header("x-api-key", "reqres-free-v1");

    // Scenario 1: Get Single User (mirrors testGetSingleUserValid)
    ScenarioBuilder getSingleUser = scenario("Get Single User")
        .exec(
            http("Get User 2")
                .get("/users/2")
                .check(status().is(200))
                .check(jsonPath("$.data.id").is("2"))
                .check(jsonPath("$.data.email").exists())
                .check(jsonPath("$.data.first_name").exists())
                .check(jsonPath("$.data.last_name").exists())
                .check(jsonPath("$.data.avatar").exists())
                .check(jsonPath("$.support.url").exists())
                .check(responseTimeInMillis().lt(2000))
        );

    // Scenario 2: Get User List with Pagination (mirrors testGetUserListWithPagination)
    ScenarioBuilder getUserList = scenario("Get User List")
        .exec(
            http("Get Users Page 1")
                .get("/users")
                .queryParam("page", "1")
                .queryParam("per_page", "6")
                .check(status().is(200))
                .check(jsonPath("$.page").is("1"))
                .check(jsonPath("$.per_page").is("6"))
                .check(jsonPath("$.data").exists())
                .check(jsonPath("$.total").exists())
                .check(jsonPath("$.total_pages").exists())
                .check(responseTimeInMillis().lt(2000))
        );

    // Scenario 3: Multiple Valid Users (mirrors testGetMultipleValidUsers with DataProvider)
    ScenarioBuilder getMultipleUsers = scenario("Get Multiple Users")
        .repeat(5, "userId").on(
            exec(session -> {
                int userId = session.getInt("userId") + 1;
                return session.set("currentUserId", userId);
            })
            .exec(
                http("Get User #{currentUserId}")
                    .get("/users/#{currentUserId}")
                    .check(status().is(200))
                    .check(jsonPath("$.data.id").exists())
                    .check(jsonPath("$.data.email").exists())
                    .check(responseTimeInMillis().lt(2000))
            )
            .pause(Duration.ofMillis(100))
        );

    // Scenario 4: Update User (mirrors testUpdateUserValid)
    ScenarioBuilder updateUser = scenario("Update User")
        .exec(
            http("Update User 2")
                .put("/users/2")
                .body(StringBody(
                    "{" +
                    "\"email\": \"morpheus.updated@example.com\"," +
                    "\"first_name\": \"Morpheus\"," +
                    "\"last_name\": \"Updated\"" +
                    "}"
                ))
                .asJson()
                .check(status().is(200))
                .check(jsonPath("$.updatedAt").exists())
                .check(responseTimeInMillis().lt(2000))
        );

    // Scenario 5: Partial Update (mirrors testPartiallyUpdateUser)
    ScenarioBuilder patchUser = scenario("Patch User")
        .exec(
            http("Patch User 2")
                .patch("/users/2")
                .body(StringBody("{\"first_name\": \"Neo\"}"))
                .asJson()
                .check(status().is(200))
                .check(jsonPath("$.first_name").is("Neo"))
                .check(jsonPath("$.updatedAt").exists())
                .check(responseTimeInMillis().lt(2000))
        );

    // Scenario 6: Delete User (mirrors testDeleteUserValid)
    ScenarioBuilder deleteUser = scenario("Delete User")
        .exec(
            http("Delete User 2")
                .delete("/users/2")
                .check(status().is(204))
                .check(responseTimeInMillis().lt(2000))
        );

    // Scenario 7: Invalid User IDs (mirrors testInvalidUserIdFormats)
    ScenarioBuilder getInvalidUsers = scenario("Get Invalid Users")
        .exec(
            http("Get Invalid User - abc")
                .get("/users/abc")
                .check(status().is(404))
        )
        .pause(Duration.ofMillis(100))
        .exec(
            http("Get Invalid User - -1")
                .get("/users/-1")
                .check(status().is(404))
        )
        .pause(Duration.ofMillis(100))
        .exec(
            http("Get Invalid User - 999")
                .get("/users/999")
                .check(status().is(404))
        );

    // Scenario 8: Mixed API Operations - Realistic user flow
    ScenarioBuilder mixedOperations = scenario("Mixed API Operations")
        .exec(
            http("Get User List")
                .get("/users?page=1&per_page=6")
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(500))
        .exec(
            http("Get Specific User")
                .get("/users/2")
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(300))
        .exec(
            http("Update User")
                .put("/users/2")
                .body(StringBody("{\"first_name\": \"Updated\", \"last_name\": \"Name\"}"))
                .asJson()
                .check(status().is(200))
        );

    // Load Simulation Setup
    {
        setUp(
            // Light load: Get single user repeatedly
            getSingleUser.injectOpen(
                rampUsers(10).during(Duration.ofSeconds(10))
            ).protocols(httpProtocol),

            // Moderate load: Get user list
            getUserList.injectOpen(
                constantUsersPerSec(2).during(Duration.ofSeconds(15))
            ).protocols(httpProtocol),

            // Stress test: Multiple users accessing different endpoints
            getMultipleUsers.injectOpen(
                atOnceUsers(5)
            ).protocols(httpProtocol),

            // Write operations with lower concurrency
            updateUser.injectOpen(
                rampUsers(5).during(Duration.ofSeconds(10))
            ).protocols(httpProtocol),

            patchUser.injectOpen(
                rampUsers(3).during(Duration.ofSeconds(8))
            ).protocols(httpProtocol),

            deleteUser.injectOpen(
                rampUsers(3).during(Duration.ofSeconds(8))
            ).protocols(httpProtocol),

            // Security/negative testing
            getInvalidUsers.injectOpen(
                atOnceUsers(2)
            ).protocols(httpProtocol),

            // Realistic mixed load
            mixedOperations.injectOpen(
                rampUsers(15).during(Duration.ofSeconds(20))
            ).protocols(httpProtocol)
        ).assertions(
            global().responseTime().max().lt(5000),           // Max response time under 5 seconds
            global().successfulRequests().percent().gt(95.0)  // 95% success rate
        );
    }
}

