package com.github.kgoedert.crm.product;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
@Tag("integration")
public class ProductResourceTest {

    @Test
    public void new_product_added_successfully(){
        JSONObject jsonObj = new JSONObject()
                             .put("phoneNumber","353837986524")
                             .put("messageContent","test");

   given()
      .port(31111) // port number
      .contentType("application/json")  //another way to specify content type
      .body(jsonObj.toString())   // use jsonObj toString method
   .when()
      .post("/testenvironment/text/send")
   .then()
      .assertThat()
      .body("message", equalTo("{\"resultMessage\":\"Message accepted\"}"));
    }
    
}