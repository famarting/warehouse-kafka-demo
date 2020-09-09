package io.famartin.warehouse;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class WarehouseResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/warehouse")
          .then()
             .statusCode(200)
             .body(is("hello"));
    }

}