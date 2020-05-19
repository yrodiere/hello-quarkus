package com.github.kgoedert.crm.product;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.vertx.core.json.JsonObject;

@QuarkusTest
@Tag("integration")
@TestInstance(Lifecycle.PER_CLASS)
public class ProductResourceITest {
   @Inject
   DataSource dataSource;

   @Test
   public void newPrductAddedSuccessfully() {
      JsonObject jsonObj = new JsonObject()
            .put("category", "GRAIN")
            .put("description", "Corn from Mexico")
            .put("name", "Corn")
            .put("price", "2.50")
            .put("stockAmount", "200");

      given()
            .port(8081)
            .contentType("application/json")
            .body(jsonObj.toString())
            .when()
            .post("/product")
            .then()
            .assertThat()
            .statusCode(201);
   }

   @Test
   public void productNameCantBeEmpty() {
      JsonObject jsonObj = new JsonObject()
            .put("category", "GRAIN")
            .put("description", "Corn from Mexico")
            .put("price", "2.50")
            .put("stockAmount", "200")
            .put("name", "");

      List<String> result = given()
            .port(8081)
            .contentType("application/json")
            .body(jsonObj.toString())
            .when()
            .post("/product")
            .then()
            .assertThat()
            .statusCode(400)
            .extract()
            .as(List.class);
      assertEquals(1, result.size());
      assertThat(result, hasItem("Name may not be blank"));
   }

   @Test
   public void productNamePriceCategoryAndStockAreRequired() {
      JsonObject prod = new JsonObject();
      List<String> result = given()
            .port(8081)
            .contentType(ContentType.JSON)
            .body(prod.toString())
            .when()
            .post("/product")
            .then()
            .assertThat()
            .statusCode(400)
            .extract()
            .as(List.class);
      assertEquals(4, result.size());
      assertThat(result, hasItem("Name may not be blank"));
      assertThat(result, hasItem("The product needs to have at least 1 item in stock"));
      assertThat(result, hasItem("The price has to be at least 0.1"));
      assertThat(result, hasItem("Category is required"));
   }

   @Test
   public void productCategoryNotAValidValue() {
      JsonObject jsonObj = new JsonObject()
            .put("category", "SALAD")
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
            .statusCode(400)
            .body(equalTo(
                  "The value 'SALAD' is not allowed for the type Category. The allowed values are: [VEGETABLES, MEAT, GRAIN, DAIRY]."));
   }

   @AfterAll
   public void cleanUp() {
      try (Connection conn = this.dataSource.getConnection(); Statement st = conn.createStatement()) {
         List<String> lines = List.of(
               "delete from orders_products",
               "delete from orders",
               "delete from products",
               "delete from customers");

         for (String line : lines) {
            st.executeUpdate(line);
         }

      } catch (SQLException e) {
         e.printStackTrace();
      }
   }

}