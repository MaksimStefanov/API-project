import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class TestApi {

 /*   Задача:
            1. Отправить get запрос для сервис Location на сайте www.metaweather.com
    с помощью Rest Assured.
    URL: https://www.metaweather.com/api/location/44418/*/

    @Test
    public void firstHomeTask() {

        given().baseUri("https://www.metaweather.com/")
                .basePath("api/location/44418/")
                .when().get()
                .then().log().body()
                .assertThat()
                .statusCode(200)
                .body("title", is("London"));
    }


    /*     1. Отправить post запрос searchSettlements (сервис для онлайн поиска
         населенных пунктов) с помощью Rest Assured
2. Проверить, что код ответа == 200
 API key: f0a8ed3fd5f618970de71afbc1d9828c*/
    @Test
    public void secondHomeTask() {
        String JSON_STRING = "{\n" +
                "\"apiKey\": \"f0a8ed3fd5f618970de71afbc1d9828c\",\n" +
                " \"modelName\": \"Address\",\n" +
                "    \"calledMethod\": \"searchSettlements\",\n" +
                "    \"methodProperties\": {\n" +
                "        \"CityName\": \"одеса\",\n" +
                "        \"Limit\": 5\n" +
                "    }\n" +
                "}";
        given().log().all().header("Content-Type", "application/json").body(JSON_STRING)
                .when().post("http://testapi.novaposhta.ua/v2.0/json/Address/searchSettlements/")
                .then().log().all()
                .assertThat()
                .statusCode(200)
                .body("errors", hasItem ("API key expired"));

    }
}
