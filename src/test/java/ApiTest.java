import org.testng.annotations.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.*;
import config.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static config.TestConfig.*;
import static io.restassured.RestAssured.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.testng.Assert.*;

public class ApiTest {

  private static final Logger LOG = LoggerFactory.getLogger(ApiTest.class);
  private RequestSpecification requestSpec;

  @BeforeClass
  public void setupClass() {
    RestAssured.baseURI = BASE_URL;
    RestAssured.basePath = API_PATH;
    requestSpec = given()
        .contentType(CONTENT_TYPE_JSON)
        .accept(CONTENT_TYPE_JSON)
        .header("x-api-key", "reqres-free-v1")
        .log()
        .ifValidationFails();

    LOG.info("=== API Test Suite Initialized ===");
    LOG.info("Base URL: {}{}", BASE_URL, API_PATH);
  }

  @Test(description = "Verify single user retrieval with valid ID", priority = 1)
  public void testGetSingleUserValid() {

    SingleUserResponse response = given()
        .spec(requestSpec)
        .pathParam("userId", VALID_USER_ID)
        .when()
        .log()
        .all()
        .get("/users/{userId}")
        .then()
        .log()
        .all()
        .statusCode(200)
        .contentType(CONTENT_TYPE_JSON)
        .extract()
        .as(SingleUserResponse.class);

    User user = response.getData();
    assertNotNull(user, "User data should not be null");
    assertEquals(user.getId()
        .intValue(), VALID_USER_ID, "User ID should match requested ID");
    assertTrue(user.getEmail()
        .contains("@reqres.in"), "Email should be from reqres.in domain");
    assertFalse(user.getFirstName()
        .isEmpty(), "First name should not be empty");
    assertFalse(user.getLastName()
        .isEmpty(), "Last name should not be empty");
    assertTrue(user.getAvatar()
        .startsWith("https://reqres.in/"), "Avatar should be from reqres.in domain");
    assertNotNull(response.getSupport(), "Support information should be present");
    assertNotNull(response.getSupport()
        .getUrl(), "Support URL should not be null");
    assertNotNull(response.getSupport()
        .getText(), "Support text should not be null");

    LOG.info("✓ Single user test passed");
  }

  @Test(description = "Verify user list retrieval with pagination", priority = 2)
  public void testGetUserListWithPagination() {
    UserListResponse response = given()
        .spec(requestSpec)
        .queryParam("page", 1)
        .queryParam("per_page", DEFAULT_PAGE_SIZE)
        .when()
        .log()
        .all()
        .get("/users")
        .then()
        .log()
        .all()
        .statusCode(200)
        .contentType(CONTENT_TYPE_JSON)
        .extract()
        .as(UserListResponse.class);

    assertEquals(response.getPage()
        .intValue(), 1, "Page number should be 1");
    assertEquals(response.getPerPage()
        .intValue(), DEFAULT_PAGE_SIZE, "Per page should match requested size");
    assertNotNull(response.getData(), "Data list should not be null");
    assertEquals(response.getData()
        .size(), DEFAULT_PAGE_SIZE, "Data list size should match per_page");
    assertTrue(response.getTotal() > 0, "Total should be greater than 0");
    assertTrue(response.getTotalPages() > 0, "Total pages should be greater than 0");

    LOG.info("✓ User list test passed ");
  }

  @DataProvider(name = "validUserIds")
  public Object[][] validUserIds() {
    return new Object[][]{{1}, {2}, {3}, {7}, {12}};
  }

  @Test(dataProvider = "validUserIds", description = "Test multiple valid user IDs")
  public void testGetMultipleValidUsers(int userId) {
    SingleUserResponse response = given()
        .spec(requestSpec)
        .pathParam("userId", userId)
        .when()
        .log()
        .all()
        .get("/users/{userId}")
        .then()
        .log()
        .all()
        .statusCode(200)
        .contentType(CONTENT_TYPE_JSON)
        .extract()
        .as(SingleUserResponse.class);

    User user = response.getData();
    assertNotNull(user, "User data should not be null for ID " + userId);
    assertEquals(user.getId()
        .intValue(), userId, "User ID should match");
    assertNotNull(user.getEmail(), "Email should not be null");
    assertNotNull(user.getFirstName(), "First name should not be null");
    assertNotNull(user.getLastName(), "Last name should not be null");
    assertNotNull(user.getAvatar(), "Avatar should not be null");

    LOG.info("✓ Valid user test passed for ID: {}", userId);
  }

  @Test(description = "Update existing user with valid data", priority = 4)
  public void testUpdateUserValid() {
    User updateUser = new User("morpheus.updated@example.com", "Morpheus", "Updated");

    Response response = given()
        .spec(requestSpec)
        .pathParam("userId", VALID_USER_ID)
        .body(updateUser)
        .when()
        .log()
        .all()
        .put("/users/{userId}")
        .then()
        .log()
        .all()
        .statusCode(200)
        .contentType(CONTENT_TYPE_JSON)
        .extract()
        .response();

    String email = response.jsonPath()
        .getString("email");
    String firstName = response.jsonPath()
        .getString("first_name");
    String lastName = response.jsonPath()
        .getString("last_name");
    String updatedAt = response.jsonPath()
        .getString("updatedAt");

    assertEquals(email, updateUser.getEmail(), "Email should match updated value");
    assertEquals(firstName, updateUser.getFirstName(), "First name should match updated value");
    assertEquals(lastName, updateUser.getLastName(), "Last name should match updated value");
    assertNotNull(updatedAt, "UpdatedAt should not be null");
    assertTrue(updatedAt.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"),
        "UpdatedAt should be in ISO 8601 format");

    LOG.info("✓ User update test passed");
  }

  @Test(description = "Partially update user with PATCH", priority = 5)
  public void testPartiallyUpdateUser() {
    String partialUpdateJson = "{\"first_name\": \"Neo\"}";

    Response response = given()
        .spec(requestSpec)
        .pathParam("userId", VALID_USER_ID)
        .body(partialUpdateJson)
        .when()
        .log()
        .all()
        .patch("/users/{userId}")
        .then()
        .log()
        .all()
        .statusCode(200)
        .contentType(CONTENT_TYPE_JSON)
        .time(lessThan((long) DEFAULT_TIMEOUT_MS), MILLISECONDS)
        .extract()
        .response();

    // POJO-based validations
    String firstName = response.jsonPath()
        .getString("first_name");
    String updatedAt = response.jsonPath()
        .getString("updatedAt");

    assertEquals(firstName, "Neo", "First name should be updated to Neo");
    assertNotNull(updatedAt, "UpdatedAt should not be null");
    assertTrue(updatedAt.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"),
        "UpdatedAt should be in ISO 8601 format");

    LOG.info("✓ Partial user update (PATCH) test passed");
  }

  @Test(description = "Delete existing user", priority = 6)
  public void testDeleteUserValid() {
    given()
        .spec(requestSpec)
        .pathParam("userId", VALID_USER_ID)
        .when()
        .log()
        .all()
        .delete("/users/{userId}")
        .then()
        .log()
        .all()
        .statusCode(204)
        .time(lessThan((long) DEFAULT_TIMEOUT_MS), MILLISECONDS);

    LOG.info("✓ User deletion test passed");
  }

  @DataProvider(name = "invalidUserIds")
  public Object[][] invalidUserIds() {
    return new Object[][]{{"abc"}, {"-1"}, {"0"}, {"999"}, {"1.5"}, {"!@#"}};
  }

  @Test(dataProvider = "invalidUserIds", description = "Test invalid user ID formats")
  public void testInvalidUserIdFormats(String invalidId) {
    given()
        .spec(requestSpec)
        .pathParam("userId", invalidId)
        .when()
        .log()
        .all()
        .get("/users/{userId}")
        .then()
        .log()
        .all()
        .statusCode(404)
        .time(lessThan((long) DEFAULT_TIMEOUT_MS), MILLISECONDS);

    LOG.info("✓ Invalid user ID test passed for ID: {}", invalidId);
  }

  @Test(description = "Test invalid pagination parameters")
  public void testInvalidPaginationParameters() {
    // Test negative page number - ReqRes returns the page as provided
    UserListResponse response1 = given()
        .spec(requestSpec)
        .queryParam("page", -1)
        .when()
        .log()
        .all()
        .get("/users")
        .then()
        .log()
        .all()
        .statusCode(200)
        .extract()
        .as(UserListResponse.class);

    assertEquals(response1.getPage()
        .intValue(), -1, "API should return page as-is for negative values");

    // Test page 0 - API defaults to page 1
    UserListResponse response2 = given()
        .spec(requestSpec)
        .queryParam("page", 0)
        .when()
        .log()
        .all()
        .get("/users")
        .then()
        .log()
        .all()
        .statusCode(200)
        .extract()
        .as(UserListResponse.class);

    assertEquals(response2.getPage()
        .intValue(), 1, "API should default page 0 to page 1");

    LOG.info("✓ Invalid pagination test passed");
  }

  @Test(description = "Test invalid data for various HTTP methods on /users/{id}")
  public void testInvalidDataOnUserEndpoint() {
    // Test PUT with invalid/empty body
    given()
        .spec(requestSpec)
        .pathParam("userId", VALID_USER_ID)
        .body("{}")
        .when()
        .log()
        .all()
        .put("/users/{userId}")
        .then()
        .log()
        .all()
        .statusCode(200); // ReqRes accepts empty body for PUT

    // Test PUT with malformed JSON
    given()
        .spec(requestSpec)
        .pathParam("userId", VALID_USER_ID)
        .body("invalid json")
        .when()
        .log()
        .all()
        .put("/users/{userId}")
        .then()
        .log()
        .all()
        .statusCode(400); // Should return 400 Bad Request

    // Test PATCH with malformed JSON
    given()
        .spec(requestSpec)
        .pathParam("userId", VALID_USER_ID)
        .body("not a json")
        .when()
        .log()
        .all()
        .patch("/users/{userId}")
        .then()
        .log()
        .all()
        .statusCode(400);

    // Test DELETE with invalid user ID
    given()
        .spec(requestSpec)
        .pathParam("userId", "invalid-id")
        .when()
        .log()
        .all()
        .delete("/users/{userId}")
        .then()
        .log()
        .all()
        .statusCode(204); 

    LOG.info("✓ Invalid data test passed for /users/{id} endpoints");
  }

  @Test(description = "Performance test - Response time validation", priority = 8)
  public void testResponseTimePerformance() {
    long startTime = System.currentTimeMillis();

    given()
        .spec(requestSpec)
        .pathParam("userId", VALID_USER_ID)
        .when()
        .log()
        .all()
        .get("/users/{userId}")
        .then()
        .log()
        .all()
        .statusCode(200)
        .time(lessThan(2000L), MILLISECONDS);

    long endTime = System.currentTimeMillis();
    long responseTime = endTime - startTime;

    assertTrue(responseTime < 2000, "Response time should be under 2 seconds, actual: " + responseTime + "ms");
    LOG.info("✓ Performance test passed - Response time: {}ms", responseTime);
  }

  @Test(description = "Test SQL injection attempts")
  public void testSqlInjectionSecurity() {
    String[] sqlInjectionPayloads = {
        "' OR '1'='1",                     // Authentication bypass
        "1; DROP TABLE users--",            // Destructive command injection
        "1' UNION SELECT * FROM users--"    // Data exfiltration via UNION
    };

    for (String payload : sqlInjectionPayloads) {
      Response response = given()
          .spec(requestSpec)
          .pathParam("userId", payload)
          .when()
          .log()
          .all()
          .get("/users/{userId}")
          .then()
          .log()
          .all()
          .extract()
          .response();

      // ReqRes API returns either 403 (Forbidden) or 404 (Not Found) for malicious input
      int statusCode = response.getStatusCode();
      assertTrue(statusCode == 403 || statusCode == 404,
          "Expected 403 or 404 for SQL injection payload, but got: " + statusCode);
    }

    LOG.info("✓ SQL injection security test passed");
  }

  @Test(description = "Verify API contract compliance using JSON Schema validation")
  public void testApiContractCompliance() {
    // Validate response against JSON Schema
    given()
        .spec(requestSpec)
        .pathParam("userId", VALID_USER_ID)
        .when()
        .log()
        .all()
        .get("/users/{userId}")
        .then()
        .log()
        .all()
        .statusCode(200)
        .contentType(CONTENT_TYPE_JSON)
        .body(matchesJsonSchemaInClasspath("schemas/single-user-schema.json"));

    LOG.info("✓ API contract compliance test passed - JSON Schema validation successful");
  }

  @AfterClass
  public void teardownClass() {
    LOG.info("=== API Test Suite Completed ===");
  }
}

