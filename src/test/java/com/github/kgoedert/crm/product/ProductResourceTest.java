package com.github.kgoedert.crm.product;

import static io.restassured.RestAssured.given;

import javax.inject.Inject;
import javax.validation.Validator;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;

@QuarkusTest
@Tag("integration")
public class ProductResourceTest {
    @Inject
    Validator validator;

    @Test
    public void productNameIsRequired() {
        JsonObject jsonObj = new JsonObject()
                .put("category", "GRAIN")
                .put("description", "Corn from Mexico")
                .put("price", "2.50")
                .put("stockAmount", "200")
                .put("name", "");

        given()
                .port(8081)
                .contentType("application/json")
                .body(jsonObj.toString())
                .when()
                .post("/product")
                .then()
                .assertThat()

                .statusCode(400);

    }

}