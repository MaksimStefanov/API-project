package config;

/**
 * Configuration class for API test settings
 */
public class TestConfig {
    public static final String BASE_URL = "https://reqres.in";
    public static final String API_PATH = "/api";

    // Timeouts
    public static final int DEFAULT_TIMEOUT_MS = 10000;

    // Test data
    public static final int VALID_USER_ID = 2;
    public static final int DEFAULT_PAGE_SIZE = 6;

    // Headers
    public static final String CONTENT_TYPE_JSON = "application/json";
}
